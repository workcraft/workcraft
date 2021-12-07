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
        queueTask(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = queueTask(we);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        if (!checkPrerequisites(we)) {
            monitor.isFinished(Result.cancel());
            return monitor;
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
                String msg = "Output persistency can currently be checked only for environment STGs without dummies.";
                if (!checkConformation && !checkDeadlock) {
                    DialogUtils.showError(msg);
                    monitor.isFinished(Result.cancel());
                    return monitor;
                } else {
                    msg += "\n\nProceed with verification of other properties?";
                    if (!DialogUtils.showConfirmWarning(msg)) {
                        monitor.isFinished(Result.cancel());
                        return monitor;
                    }
                    checkPersistency = false;
                }
            }
        } else {
            String msg = envFile == null ? ""
                    : "Cannot read an STG model from the file:\n" + envFile.getAbsolutePath() + "\n\n";

            if (checkConformation) {
                msg += "The circuit conformation cannot be checked without environment STG.\n";
                if (!checkDeadlock && !checkPersistency) {
                    DialogUtils.showError(msg);
                } else {
                    msg += "Proceed with verification of the other properties?\n";
                    boolean proceed = DialogUtils.showConfirmWarning(msg);
                    checkDeadlock &= proceed;
                    checkPersistency &= proceed;
                }
                checkConformation = false;
            }
        }
        if (!checkConformation && !checkDeadlock && !checkPersistency) {
            monitor.isFinished(Result.cancel());
            return monitor;
        }

        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        CheckTask task = new CheckTask(we, checkConformation, checkDeadlock, checkPersistency);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
        return monitor;
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we)
            && VerificationUtils.checkCircuitHasComponents(we)
            && VerificationUtils.checkBlackboxComponents(we)
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
