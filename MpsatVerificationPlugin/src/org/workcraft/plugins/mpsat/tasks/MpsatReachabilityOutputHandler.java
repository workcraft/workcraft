package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Triple;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatReachabilityOutputHandler implements Runnable {

    private final WorkspaceEntry we;
    private final PcompOutput pcompOutput;
    private final MpsatOutput mpsatOutput;
    private final MpsatParameters settings;

    MpsatReachabilityOutputHandler(WorkspaceEntry we, PcompOutput pcompOutput, MpsatOutput mpsatOutput, MpsatParameters settings) {
        this.we = we;
        this.pcompOutput = pcompOutput;
        this.mpsatOutput = mpsatOutput;
        this.settings = settings;
    }

    private String getMessage(boolean isSatisfiable) {
        String propertyName = "Property";
        if ((settings.getName() != null) && !settings.getName().isEmpty()) {
            propertyName = settings.getName();
        }
        boolean inversePredicate = settings.getInversePredicate();
        String propertyStatus = isSatisfiable == inversePredicate ? " is violated." : " holds.";
        return propertyName + propertyStatus;
    }

    private MpsatSolution improveConsistencySolution(MpsatSolution solution) {
        Trace trace = getProjectedTrace(solution.getMainTrace());
        LogUtils.logMessage("Processing consistency violation trace: ");
        LogUtils.logMessage("  reported: " + solution.getMainTrace());
        LogUtils.logMessage("  projected: " + trace);
        int size = trace.size();
        if (size > 0) {
            String lastTransitionRef = trace.get(size - 1);
            final Triple<String, Direction, Integer> r = LabelParser.parseSignalTransition(lastTransitionRef);
            if (r != null) {
                String signalRef = r.getFirst();
                solution.setComment("Signal '" + signalRef + "' is inconsistent");
            }
        }
        return solution;
    }

    private MpsatSolution improveOutputPersistencySolution(MpsatSolution solution) {
        StgModel stg = getSrcStg();
        if ((solution == null) || (stg == null)) {
            return solution;
        }
        Trace trace = getProjectedTrace(solution.getMainTrace());
        LogUtils.logMessage("Processing output percistency violation trace: ");
        LogUtils.logMessage("  reported: " + solution.getMainTrace());
        LogUtils.logMessage("  projected: " + trace);
        String comment = null;
        if (!fireTrace(stg, trace)) {
            LogUtils.logWarning("Cannot execute projected output persistency violation trace: " + trace);
        } else {
            HashSet<String> enabledLocalSignals = getEnabledLocalSignals(stg);
            for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> nonpersistentLocalSignals = new HashSet<>(enabledLocalSignals);
                nonpersistentLocalSignals.remove(transition.getSignalName());
                nonpersistentLocalSignals.removeAll(getEnabledLocalSignals(stg));
                if (!nonpersistentLocalSignals.isEmpty()) {
                    String signalList = ReferenceHelper.getReferencesAsString(nonpersistentLocalSignals);
                    if (nonpersistentLocalSignals.size() > 1) {
                        solution.setComment("Non-persistent signals " + signalList);
                    } else {
                        solution.setComment("Non-persistent signal '" + signalList + "'");
                    }
                    trace.add(stg.getNodeReference(transition));
                    LogUtils.logMessage("  extended: " + trace);
                    break;
                }
                stg.unFire(transition);
            }
        }
        return new MpsatSolution(trace, null, comment);
    }

    private HashSet<SignalTransition> getEnabledSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (SignalTransition transition: stg.getSignalTransitions()) {
            if (stg.isEnabled(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    private HashSet<String> getEnabledLocalSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
            if ((transition.getSignalType() == Type.OUTPUT) || (transition.getSignalType() == Type.INTERNAL)) {
                result.add(transition.getSignalName());
            }
        }
        return result;
    }

    private MpsatSolution improveConformationSolution(MpsatSolution solution) {
        StgModel stg = getSrcStg();
        if ((solution == null) || (stg == null)) {
            return solution;
        }
        Trace trace = getProjectedTrace(solution.getMainTrace());
        LogUtils.logMessage("Processing conformation violation trace: ");
        LogUtils.logMessage("  reported: " + solution.getMainTrace());
        LogUtils.logMessage("  projected: " + trace);
        String comment = null;
        if (!fireTrace(stg, trace)) {
            LogUtils.logWarning("Cannot execute projected conformation violation trace: " + trace);
        } else {
            for (SignalTransition transition: stg.getSignalTransitions()) {
                if (stg.isEnabled(transition) && (transition.getSignalType() == Type.OUTPUT)) {
                    String signal = transition.getSignalName();
                    trace.add(stg.getNodeReference(transition));
                    LogUtils.logMessage("  extended: " + trace);
                    comment = "Unexpected change of output '" + signal + "'";
                    break;
                }
            }
        }
        return new MpsatSolution(trace, null, comment);
    }

    private StgModel getSrcStg() {
        StgModel result = null;
        File file = null;
        if (pcompOutput != null) {
            File[] inputFiles = pcompOutput.getInputFiles();
            if ((inputFiles != null) && (inputFiles.length > 0)) {
                file = inputFiles[0];
            }
        }
        if (file != null) {
            try {
                FileInputStream is = new FileInputStream(file);
                StgImporter importer = new StgImporter();
                result = importer.importStg(is);
            } catch (DeserialisationException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private Trace getProjectedTrace(Trace trace) {
        if (pcompOutput == null) {
            return trace;
        }
        Trace result = new Trace();
        File detailFile = pcompOutput.getDetailFile();
        try {
            CompositionData compositionData = new CompositionData(detailFile);
            ComponentData componentData = compositionData.getComponentData(0);
            for (String ref: trace) {
                String srcRef = componentData.getSrcTransition(ref);
                if (srcRef != null) {
                    result.add(srcRef);
                }
            }
        } catch (FileNotFoundException e) {
            return trace;
        }
        return result;
    }

    private boolean fireTrace(StgModel stg, Trace trace) {
        for (String ref: trace) {
            Node node = stg.getNodeByReference(ref);
            if (node instanceof Transition) {
                Transition transition = (Transition) node;
                if (stg.isEnabled(transition)) {
                    stg.fire(transition);
                } else {
                    LogUtils.logError("Trace transition '" + ref + "' is not enabled.");
                    return false;
                }
            } else {
                LogUtils.logError("Trace transition '" + ref + "' cannot be found.");
                return false;
            }
        }
        return true;
    }

    private List<MpsatSolution> getSolutions() {
        List<MpsatSolution> result = new LinkedList<>();
        MpsatOutoutParser mrp = new MpsatOutoutParser(mpsatOutput);
        List<MpsatSolution> solutions = mrp.getSolutions();
        if (settings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY) {
            for (MpsatSolution solution: solutions) {
                result.add(improveConsistencySolution(solution));
            }
        } else if (settings.getMode() == MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY) {
            for (MpsatSolution solution: solutions) {
                result.add(improveOutputPersistencySolution(solution));
            }
        } else if (settings.getMode() == MpsatMode.STG_REACHABILITY_CONFORMATION) {
            for (MpsatSolution solution: solutions) {
                result.add(improveConformationSolution(solution));
            }
        } else {
            result.addAll(solutions);
        }
        return result;
    }

    @Override
    public void run() {
        List<MpsatSolution> solutions = getSolutions();
        String title = "Verification results";
        String message = getMessage(!solutions.isEmpty());
        Framework framework = Framework.getInstance();
        if (!MpsatUtils.hasTraces(solutions)) {
            DialogUtils.showInfo(message, title);
        } else if (framework.isInGuiMode()) {
            String traceInfo = "";
            if (settings.getMode() != MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY) {
                String traceCharacteristic = settings.getInversePredicate() ? "problematic" : "sought";
                traceInfo = "&#160;Trace(s) leading to the " + traceCharacteristic + " state(s):<br><br>";
            }
            String extendedMessage = "<html><br>&#160;" + message + "<br><br>" + traceInfo + "</html>";
            MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, title, extendedMessage, solutions);
            MainWindow mainWindow = framework.getMainWindow();
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        }
    }

}
