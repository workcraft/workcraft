package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.CheckTask;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class CombinedVerificationCommand extends AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "All of the above (reuse unfolding) [MPSat]";
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
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        queueVerification(we, monitor);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        queueVerification(we, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueVerification(WorkspaceEntry we, VerificationChainResultHandlingMonitor monitor) {
        if (!checkPrerequisites(we)) {
            monitor.isFinished(Result.failure());
            return;
        }
        // Adjust the set of checked properties depending on availability of environment STG
        boolean checkConformation = checkConformation();
        boolean checkDeadlock = checkDeadlock();
        boolean checkPersistency = checkPersistency();

        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        File envFile = circuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg != null) {
            boolean noDummies = envStg.getDummyTransitions().isEmpty();
            if (!noDummies && checkPersistency) {
                if (!checkConformation && !checkDeadlock) {
                    DialogUtils.showError("Output persistency can currently be checked only for environment STGs without dummies.");
                    monitor.isFinished(Result.failure());
                    return;
                } else {
                    String msg = "Output persistency can currently be checked only for environment STGs without dummies.\n\n" +
                            "Proceed with verification of other properties?";
                    if (!DialogUtils.showConfirmWarning(msg, "Verification", true)) {
                        monitor.isFinished(Result.failure());
                        return;
                    }
                    checkPersistency = false;
                }
            }
        } else {
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
                                    + "Proceed with verification of the other properties?\n");
                    checkDeadlock &= proceed;
                    checkPersistency &= proceed;
                }
                checkConformation = false;
            }
        }
        if (!checkConformation && !checkDeadlock && !checkPersistency) {
            monitor.isFinished(Result.failure());
            return;
        }
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CheckTask task = new CheckTask(we, checkConformation, checkDeadlock, checkPersistency);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we)
            && VerificationUtils.checkCircuitHasComponents(we)
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
