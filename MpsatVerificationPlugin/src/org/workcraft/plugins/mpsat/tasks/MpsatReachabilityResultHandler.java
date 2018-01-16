package org.workcraft.plugins.mpsat.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSolution;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.Result;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Triple;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatReachabilityResultHandler implements Runnable {

    private final WorkspaceEntry we;
    private final Result<? extends ExternalProcessResult> result;
    private final MpsatParameters settings;

    MpsatReachabilityResultHandler(WorkspaceEntry we, Result<? extends ExternalProcessResult> result, MpsatParameters settings) {
        this.we = we;
        this.result = result;
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

    private StgModel getInputStg() {
        final byte[] content = result.getReturnValue().getFileData(MpsatTask.FILE_NET_G);
        if (content == null) {
            return null;
        }
        try {
            StgImporter importer = new StgImporter();
            return importer.importStg(new ByteArrayInputStream(content));
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    private HashSet<StgPlace> getDevPlaces(StgModel stg) {
        HashSet<StgPlace> devPlaces = new HashSet<>();
        final byte[] content = result.getReturnValue().getFileData(MpsatTask.FILE_PLACES);
        if (content != null) {
            InputStream is = new ByteArrayInputStream(content);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            try {
                // First line is for device places, second - for environment places.
                line = br.readLine();
                for (String ref: line.trim().split("\\s")) {
                    Node node = stg.getNodeByReference(ref);
                    if (node instanceof StgPlace) {
                        devPlaces.add((StgPlace) node);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return devPlaces;
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

    private HashSet<SignalTransition> getEnabledSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (SignalTransition transition: stg.getSignalTransitions()) {
            if (stg.isEnabled(transition)) {
                result.add(transition);
            }
        }
        return result;
    }

    private HashSet<SignalTransition> getDisabledOutputTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (SignalTransition transition: stg.getSignalTransitions()) {
            if ((transition.getSignalType() == Type.OUTPUT) && !stg.isEnabled(transition)) {
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

    private HashSet<String> getEnabledOutputSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
            if (transition.getSignalType() == Type.OUTPUT) {
                result.add(transition.getSignalName());
            }
        }
        return result;
    }

    private void improveConsistencySolution(MpsatSolution solution) {
        Trace mainTrace = solution.getMainTrace();
        int size = mainTrace.size();
        if (size > 0) {
            String lastTransitionRef = mainTrace.get(size - 1);
            final Triple<String, Direction, Integer> r = LabelParser.parseSignalTransition(lastTransitionRef);
            if (r != null) {
                String signalRef = r.getFirst();
                solution.setComment("Signal '" + signalRef + "' is inconsistent");
            }
        }
    }

    private void improveOutputPersistencySolution(MpsatSolution solution) {
        StgModel stg = getInputStg();
        if ((solution != null) && (stg != null)) {
            Trace mainTrace = solution.getMainTrace();
            if (!fireTrace(stg, mainTrace)) {
                LogUtils.logWarning("Cannot execute output persistency trace: " + mainTrace);
            } else {
                LogUtils.logMessage("Extending output persistency violation trace: ");
                LogUtils.logMessage("  original:" + mainTrace);
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
                        mainTrace.add(stg.getNodeReference(transition));
                        break;
                    }
                    stg.unFire(transition);
                }
                LogUtils.logMessage("  extended:" + mainTrace);
            }
        }
    }

    private void improveConformationSolution(MpsatSolution solution) {
        StgModel stg = getInputStg();
        HashSet<StgPlace> devPlaces = getDevPlaces(stg);
        if ((solution != null) && (stg != null)) {
            Trace mainTrace = solution.getMainTrace();
            if (!fireTrace(stg, mainTrace)) {
                LogUtils.logWarning("Cannot execute conformation violation trace: " + mainTrace);
            } else {
                LogUtils.logMessage("Extending conformation violation trace: ");
                LogUtils.logMessage("  original:" + mainTrace);
                HashSet<String> enabledOutputSignals = getEnabledOutputSignals(stg);
                for (SignalTransition transition: getDisabledOutputTransitions(stg)) {
                    String signalName = transition.getSignalName();
                    if (!enabledOutputSignals.contains(signalName)) {
                        boolean isDevEnabled = true;
                        for (Node predNode: stg.getPreset(transition)) {
                            if (predNode instanceof StgPlace) {
                                StgPlace predPlace = (StgPlace) predNode;
                                if ((predPlace.getTokens() == 0) && devPlaces.contains(predPlace)) {
                                    isDevEnabled = false;
                                    break;
                                }
                            }
                        }
                        if (isDevEnabled) {
                            String signal = transition.getSignalName();
                            solution.setComment("Unexpected change of output '" + signal + "'");
                            mainTrace.add(stg.getNodeReference(transition));
                            break;
                        }
                    }
                }
                LogUtils.logMessage("  extended:" + mainTrace);
            }
        }
    }

    @Override
    public void run() {
        MpsatResultParser mrp = new MpsatResultParser(result.getReturnValue());
        List<MpsatSolution> solutions = mrp.getSolutions();
        boolean isConsistency = settings.getMode() == MpsatMode.STG_REACHABILITY_CONSISTENCY;
        boolean isOutputPersistency = settings.getMode() == MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY;
        boolean isConformation = settings.getMode() == MpsatMode.STG_REACHABILITY_CONFORMATION;
        if (isConsistency) {
            for (MpsatSolution solution: solutions) {
                improveConsistencySolution(solution);
            }
        } else if (isOutputPersistency) {
            for (MpsatSolution solution: solutions) {
                improveOutputPersistencySolution(solution);
            }
        } else if (isConformation) {
            for (MpsatSolution solution: solutions) {
                improveConformationSolution(solution);
            }
        }
        String title = "Verification results";
        String message = getMessage(!solutions.isEmpty());
        Framework framework = Framework.getInstance();
        if (!MpsatUtils.hasTraces(solutions)) {
            DialogUtils.showInfo(message, title);
        } else if (framework.isInGuiMode()) {
            String traceInfo = "";
            if (!isOutputPersistency) {
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
