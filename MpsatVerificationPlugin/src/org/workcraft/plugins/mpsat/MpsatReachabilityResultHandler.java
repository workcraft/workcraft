package org.workcraft.plugins.mpsat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.MpsatReachibilityDialog;
import org.workcraft.plugins.mpsat.gui.MpsatSolution;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatReachabilityResultHandler implements Runnable {

    private final WorkspaceEntry we;
    private final Result<? extends ExternalProcessResult> result;
    private final MpsatSettings settings;

    MpsatReachabilityResultHandler(WorkspaceEntry we, Result<? extends ExternalProcessResult> result, MpsatSettings settings) {
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
            DotGImporter importer = new DotGImporter();
            return importer.importSTG(new ByteArrayInputStream(content));
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
            String flatRef = NamespaceHelper.hierarchicalToFlatName(ref);
            Node node = stg.getNodeByReference(flatRef);
            if (node instanceof Transition) {
                Transition transition = (Transition) node;
                if (stg.isEnabled(transition)) {
                    stg.fire(transition);
                } else {
                    LogUtils.logErrorLine("Trace transition '" + flatRef + "' is not enabled.");
                    return false;
                }
            } else {
                LogUtils.logErrorLine("Trace transition '" + flatRef + "' cannot be found.");
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

    private void improveOutputPersistencySolution(MpsatSolution solution) {
        StgModel stg = getInputStg();
        if ((solution != null) && (stg != null)) {
            Trace mainTrace = solution.getMainTrace();
            if (!fireTrace(stg, mainTrace)) {
                LogUtils.logWarningLine("Cannot execute output persistency trace: " + mainTrace);
            } else {
                LogUtils.logMessageLine("Extending output persistency violation trace: ");
                LogUtils.logMessageLine("  original:" + mainTrace);
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
                LogUtils.logMessageLine("  extended:" + mainTrace);
            }
        }
    }

    private void improveConformationSolution(MpsatSolution solution) {
        StgModel stg = getInputStg();
        HashSet<StgPlace> devPlaces = getDevPlaces(stg);
        if ((solution != null) && (stg != null)) {
            Trace mainTrace = solution.getMainTrace();
            if (!fireTrace(stg, mainTrace)) {
                LogUtils.logWarningLine("Cannot execute conformation violation trace: " + mainTrace);
            } else {
                LogUtils.logMessageLine("Extending conformation violation trace: ");
                LogUtils.logMessageLine("  original:" + mainTrace);
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
                LogUtils.logMessageLine("  extended:" + mainTrace);
            }
        }
    }

    @Override
    public void run() {
        MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
        List<MpsatSolution> solutions = mdp.getSolutions();
        boolean isOutputPersistency = settings.getMode() == MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY;
        boolean isConformation = settings.getMode() == MpsatMode.STG_REACHABILITY_CONFORMATION;
        if (isOutputPersistency) {
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
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (MpsatSolution.hasTraces(solutions)) {
            String traceInfo = "";
            if (!isOutputPersistency) {
                String traceChearacteristic = settings.getInversePredicate() ? "problematic" : "sought";
                traceInfo = "&#160;Trace(s) leading to the " + traceChearacteristic + " state(s):<br><br>";
            }
            String extendedMessage = "<html><br>&#160;" + message + "<br><br>" + traceInfo + "</html>";
            final MpsatReachibilityDialog solutionsDialog = new MpsatReachibilityDialog(we, title, extendedMessage, solutions);
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
