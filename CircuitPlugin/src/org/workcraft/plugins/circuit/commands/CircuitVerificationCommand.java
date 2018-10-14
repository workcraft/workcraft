package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tasks.CircuitCheckTask;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.io.File;

public class CircuitVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Conformation, deadlock freeness, and output persistency (reuse unfolding) [MPSat]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueVerification(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        MpsatChainResultHandler monitor = queueVerification(we);
        Result<? extends MpsatChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private MpsatChainResultHandler queueVerification(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        // Adjust the set of checked properties depending on availability of environment STG
        boolean checkConformation = checkConformation();
        boolean checkDeadlock = checkDeadlock();
        boolean checkPersistency = checkPersistency();
        VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        File envFile = visualCircuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            String messagePrefix = "";
            if (envFile != null) {
                messagePrefix = "Cannot read an STG model from the file:\n" + envFile.getAbsolutePath() + "\n\n";
            }
            if (checkConformation) {
                if (!checkDeadlock && !checkPersistency) {
                    DialogUtils.showError(messagePrefix + "The circuit conformation cannot be checked without environment STG.\n");
                } else {
                    boolean proceed = DialogUtils.showConfirmWarning(messagePrefix
                                    + "The circuit conformation cannot be checked without environment STG.\n"
                                    + "Proceed with verification of the other properties?\n",
                            "Circuit verification", true);
                    checkDeadlock &= proceed;
                    checkPersistency &= proceed;
                }
                checkConformation = false;
            }
        }
        if (!checkConformation && !checkDeadlock && !checkPersistency) {
            return null;
        }
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CircuitCheckTask task = new CircuitCheckTask(we, checkConformation, checkDeadlock, checkPersistency);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return VerificationUtils.checkCircuitHasComponents(we)
            && VerificationUtils.checkInterfaceInitialState(we)
            && VerificationUtils.checkInterfaceConstrains(we, true);
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
