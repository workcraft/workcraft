package org.workcraft.plugins.mpsat.commands;

import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.gui.NwayDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatNwayConformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatNwayConformationVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "N-way conformation (without dummies) [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            LogUtils.logError("Command '" + getClass().getSimpleName() + "' only works in GUI mode.");
        } else {
            MainWindow mainWindow = framework.getMainWindow();
            NwayDialog dialog = new NwayDialog(mainWindow);
            GUI.centerToParent(dialog, mainWindow);
            if (dialog.run()) {
                Workspace workspace = framework.getWorkspace();
                ArrayList<WorkspaceEntry> wes = new ArrayList<>();
                for (Path<String> path : dialog.getSourcePaths()) {
                    wes.add(workspace.getWork(path));
                }

                MpsatNwayConformationTask task = new MpsatNwayConformationTask(wes);
                TaskManager manager = framework.getTaskManager();
                String description = MpsatUtils.getToolchainDescription(we.getTitle());
                MpsatChainResultHandler monitor = new MpsatChainResultHandler(we);
                manager.queue(task, description, monitor);
            }
        }
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        ScriptableCommandUtils.showErrorRequiresGui(getClass().getSimpleName());
        return null;
    }

}