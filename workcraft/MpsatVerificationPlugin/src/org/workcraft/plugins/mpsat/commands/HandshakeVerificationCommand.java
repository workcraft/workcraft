package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.gui.HandshakeWizardDialog;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class HandshakeVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "Handshake wizard [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);

        HandshakeWizardDialog dialog = new HandshakeWizardDialog(mainWindow, stg);
        if (dialog.reveal()) {
            TaskManager manager = framework.getTaskManager();
            VerificationChainTask task = new VerificationChainTask(we, dialog.getSettings());
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
            manager.queue(task, description, monitor);
        }
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        ScriptableCommandUtils.showErrorRequiresGui(getClass());
        return null;
    }

}
