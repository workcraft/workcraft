package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.ReachibilityDialog;
import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
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
        final byte[] content = result.getReturnValue().getFileData(MpsatTask.FILE_MPSAT_G_INPUT);
        if (content == null) {
            return null;
        }
        try {
            return new DotGImporter().importSTG(new ByteArrayInputStream(content));
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    private void fireTrace(StgModel stg, Trace trace) {
        for (String reference: trace) {
            Node node = stg.getNodeByReference(reference);
            if (node instanceof Transition) {
                Transition transition = (Transition) node;
                if (stg.isEnabled(transition)) {
                    stg.fire(transition);
                }
            }
        }
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

    private HashSet<String> getEnabledOutputSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
            if (transition.getSignalType() != Type.INPUT) {
                result.add(transition.getSignalName());
            }
        }
        return result;
    }

    private void setOutputPersistencyComment(Solution solution) {
        if (solution != null) {
            StgModel stg = getInputStg();
            fireTrace(stg, solution.getMainTrace());
            HashSet<String> enabledSignals = getEnabledOutputSignals(stg);
            HashSet<String> nonpersistentSignals = new HashSet<>();
            for (SignalTransition transition: getEnabledSignalTransitions(stg)) {
                stg.fire(transition);
                HashSet<String> signals = new HashSet<>(enabledSignals);
                signals.remove(transition.getSignalName());
                signals.removeAll(getEnabledOutputSignals(stg));
                nonpersistentSignals.addAll(signals);
            }
            if (!nonpersistentSignals.isEmpty()) {
                String comment = "<html>Non-persistent signal(s) <b>" +
                        ReferenceHelper.getReferencesAsString(nonpersistentSignals) +
                        "</b></html>";
                solution.setComment(comment);
            }
        }
    }

    @Override
    public void run() {
        MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
        List<Solution> solutions = mdp.getSolutions();
        boolean isOutputPersistency = settings.getMode() == MpsatMode.STG_REACHABILITY_OUTPUT_PERSISTENCY;
        if (isOutputPersistency) {
            for (Solution solution: solutions) {
                setOutputPersistencyComment(solution);
            }
        }
        String title = "Verification results";
        String message = getMessage(!solutions.isEmpty());
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (Solution.hasTraces(solutions)) {
            String extraMessage = isOutputPersistency ? "" : "&#160;Trace(s) leading to the problematic state(s):<br><br>";
            String extendedMessage = "<html><br>&#160;" + message + "<br><br>" + extraMessage + "</html>";
            final ReachibilityDialog solutionsDialog = new ReachibilityDialog(we, title, extendedMessage, solutions);
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
