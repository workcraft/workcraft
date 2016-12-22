package org.workcraft.plugins.circuit.commands;

import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractVerificationCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tasks.CheckCircuitTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Circuit vetification";

    public String getDisplayName() {
        return "Conformation, deadlock and output persistency (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();

        Circuit circuit = (Circuit) we.getModelEntry().getMathModel();
        if (circuit.getFunctionComponents().isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "The circuit must have components.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean checkConformation = checkConformation();
        boolean checkDeadlock = checkDeadlock();
        boolean checkPersistency = checkPersistency();

        VisualCircuit visualCircuit = (VisualCircuit) we.getModelEntry().getVisualModel();
        File envFile = visualCircuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            String messagePrefix = "";
            if (envFile != null) {
                messagePrefix = "Cannot read an STG model from the file:\n" + envFile.getAbsolutePath() + "\n\n";
            }
            if (checkConformation) {
                if (checkDeadlock || checkPersistency) {
                    int answer = JOptionPane.showConfirmDialog(mainWindow, "Warning: " + messagePrefix
                            + "The circuit conformation cannot be checked without environment STG.\n"
                            + "Proceed with verification of the other properties?\n",
                            TITLE, JOptionPane.YES_NO_OPTION);

                    boolean proceed = answer == JOptionPane.YES_OPTION;
                    checkDeadlock &= proceed;
                    checkPersistency &= proceed;
                } else {
                    JOptionPane.showMessageDialog(mainWindow, "Error: " + messagePrefix
                            + "The circuit conformation cannot be checked without environment STG.\n",
                            TITLE, JOptionPane.ERROR_MESSAGE);
                    return;
                }
                checkConformation = false;
            } else {
                JOptionPane.showMessageDialog(mainWindow, "Warning: " + messagePrefix
                        + "The circuit will be verified without environment STG.\n",
                        TITLE, JOptionPane.WARNING_MESSAGE);
            }
        }

        if (checkConformation || checkDeadlock || checkPersistency) {
            final CheckCircuitTask task = new CheckCircuitTask(we, checkConformation, checkDeadlock, checkPersistency);
            String description = "MPSat tool chain";
            String title = we.getTitle();
            if (!title.isEmpty()) {
                description += "(" + title + ")";
            }
            final TaskManager taskManager = framework.getTaskManager();
            final MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
            taskManager.queue(task, description, monitor);
        }
    }

    public boolean checkConformation() {
        return true;
    }

    public boolean checkDeadlock() {
        return true;
    }

    public boolean checkPersistency() {
        return true;
    }

}
