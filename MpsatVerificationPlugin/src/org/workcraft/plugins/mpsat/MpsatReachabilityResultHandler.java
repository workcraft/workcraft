package org.workcraft.plugins.mpsat;

import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.gui.ReachibilityDialog;
import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
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
        if ((settings != null) && (settings.getName() != null) && !settings.getName().isEmpty()) {
            propertyName = settings.getName();
        }
        boolean inversePredicate = true;
        if (settings != null) {
            inversePredicate = settings.getInversePredicate();
        }
        String propertyStatus = isSatisfiable == inversePredicate ? " is violated." : " holds.";
        return (propertyName + propertyStatus);
    }

    @Override
    public void run() {
        MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
        List<Solution> solutions = mdp.getSolutions();
        String title = "Verification results";
        String message = getMessage(!solutions.isEmpty());
        if (Solution.hasTraces(solutions)) {
            String extendedMessage = "<html><br>&#160;" + message +  "<br><br>&#160;Trace(s) leading to the problematic state(s):<br><br></html>";
            final ReachibilityDialog dialog = new ReachibilityDialog(we, title, extendedMessage, solutions);
            GUI.centerToParent(dialog, Framework.getInstance().getMainWindow());
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
