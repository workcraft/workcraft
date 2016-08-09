package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Trace;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.ReachibilityDialog;
import org.workcraft.plugins.mpsat.gui.Solution;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
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
        final byte[] content = result.getReturnValue().getFileContent(MpsatTask.FILE_MPSAT_G_INPUT);
        if (content == null) {
            return null;
        }
        try {
            return new DotGImporter().importSTG(new ByteArrayInputStream(content));
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
        List<Solution> solutions = new LinkedList<>();
        boolean needExtraMessage = false;
        for (Solution solution: mdp.getSolutions()) {
            Trace mainTrace = solution.getMainTrace();
            Trace branchTrace = solution.getBranchTrace();
            String comment = null;
            String signalName = mainTrace.get(mainTrace.size() - 1);
            switch (settings.getMode()) {
            case STG_REACHABILITY_OUTPUT_PERSISTENCY:
                comment = "<html>Signal <b>" + signalName + "</b> is disabled after the following trace:</html>";
                break;
            case STG_REACHABILITY_CONFORMATION:
                comment = "<html>Unexpected signal <b>" + signalName + "</b> after the following trace:</html>";
                break;
            default:
                comment = solution.getComment();
                needExtraMessage = true;
                break;
            }
            solutions.add(new Solution(mainTrace, branchTrace, comment));
        }
        String title = "Verification results";
        String message = getMessage(!solutions.isEmpty());
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if (Solution.hasTraces(solutions)) {
            String extraMessage = needExtraMessage ? "&#160;Trace(s) leading to the problematic state(s):<br><br>" : "";
            String extendedMessage = "<html><br>&#160;" + message + "<br><br>" + extraMessage + "</html>";
            final ReachibilityDialog solutionsDialog = new ReachibilityDialog(we, title, extendedMessage, solutions);
            GUI.centerToParent(solutionsDialog, mainWindow);
            solutionsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(mainWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
