package org.workcraft.plugins.circuit.tools;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.CheckCircuitTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckCircuitTool extends VerificationTool {

    public String getDisplayName() {
        return "Conformation, deadlock and hazard (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Circuit;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();

        Circuit circuit = (Circuit) we.getModelEntry().getMathModel();
        if (circuit.getFunctionComponents().isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "The circuit must have components.",
                    "Circuit verification error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final CheckCircuitTask task = new CheckCircuitTask(we, checkConformation(), checkDeadlock(), checkHazard());
        String description = "MPSat tool chain";
        String title = we.getTitle();
        if (!title.isEmpty()) {
            description += "(" + title +")";
        }
        framework.getTaskManager().queue(task, description, new MpsatChainResultHandler(task));
    }

    public boolean checkConformation() {
        return true;
    }

    public boolean checkDeadlock() {
        return true;
    }

    public boolean checkHazard() {
        return true;
    }

}
