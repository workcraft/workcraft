package org.workcraft.plugins.circuit.commands;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tasks.CheckCircuitTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Conformation, deadlock and output persistency (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }
    @Override
    public void run(WorkspaceEntry we) {
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        MpsatChainResultHandler monitor = queueVerification(we);
        Result<? extends MpsatChainResult> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private MpsatChainResultHandler queueVerification(WorkspaceEntry we) {
        MpsatChainResultHandler monitor = null;
        boolean checkConformation = checkConformation();
        boolean checkDeadlock = checkDeadlock();
        boolean checkPersistency = checkPersistency();
        if (identifyChecks(we, checkConformation, checkDeadlock, checkPersistency)) {
            if (checkConformation || checkDeadlock || checkPersistency) {
                Framework framework = Framework.getInstance();
                TaskManager manager = framework.getTaskManager();
                CheckCircuitTask task = new CheckCircuitTask(we, checkConformation, checkDeadlock, checkPersistency);
                String description = MpsatUtils.getToolchainDescription(we.getTitle());
                monitor = new MpsatChainResultHandler(task);
                manager.queue(task, description, monitor);
            }
        }
        return monitor;
    }

    public boolean checkDeadlock() {
        return true;
    }

    public boolean checkPersistency() {
        return true;
    }

    public boolean checkConformation() {
        return true;
    }

    private boolean identifyChecks(WorkspaceEntry we, boolean checkConformation, boolean checkDeadlock, boolean checkPersistency) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (circuit.getFunctionComponents().isEmpty()) {
            DialogUtils.showError("The circuit must have components.");
            return false;
        }

        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        File envFile = visualCircuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            String messagePrefix = "";
            if (envFile != null) {
                messagePrefix = "Cannot read an STG model from the file:\n" + envFile.getAbsolutePath() + "\n\n";
            }
            if (checkConformation) {
                if (checkDeadlock || checkPersistency) {
                    boolean proceed = DialogUtils.showConfirmWarning(messagePrefix
                            + "The circuit conformation cannot be checked without environment STG.\n"
                            + "Proceed with verification of the other properties?\n",
                            "Circuit verification");
                    checkDeadlock &= proceed;
                    checkPersistency &= proceed;
                } else {
                    DialogUtils.showError(messagePrefix + "The circuit conformation cannot be checked without environment STG.\n");
                    return false;
                }
                checkConformation = false;
            } else {
                DialogUtils.showWarning(messagePrefix + "The circuit will be verified without environment STG.\n");
            }
        }
        return true;
    }

}
