package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat_verification.gui.NwayConformationDialog;
import org.workcraft.plugins.mpsat_verification.tasks.NwayConformationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.NwayConformationTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NwayConformationVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    @Override
    public String getDisplayName() {
        return "N-way conformation [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return (we == null) || WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        List<WorkspaceEntry> wes = new ArrayList<>();
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();

        MainWindow mainWindow = framework.getMainWindow();
        NwayConformationDialog dialog = new NwayConformationDialog(mainWindow);
        if (dialog.reveal()) {
            Set<Path<String>> paths = dialog.getSourcePaths();
            if (paths != null) {
                for (Path<String> path : paths) {
                    wes.add(workspace.getWork(path));
                }
            }

            NwayConformationChainResultHandlingMonitor monitor = new NwayConformationChainResultHandlingMonitor(wes);
            queueVerification(wes, monitor);
        }
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        List<WorkspaceEntry> wes = new ArrayList<>();
        Workspace workspace = Framework.getInstance().getWorkspace();
        for (WorkspaceEntry work: workspace.getWorks()) {
            if (WorkspaceUtils.isApplicable(work, StgModel.class)) {
                wes.add(work);
            }
        }

        NwayConformationChainResultHandlingMonitor monitor = new NwayConformationChainResultHandlingMonitor(wes);
        queueVerification(wes, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueVerification(List<WorkspaceEntry> wes, NwayConformationChainResultHandlingMonitor monitor) {
        if (wes.size() < 2) {
            DialogUtils.showWarning("At least two STGs are required for N-way conformation.");
            monitor.isFinished(Result.failure());
            return;
        }

        NwayConformationTask task = new NwayConformationTask(wes);
        TaskManager manager = Framework.getInstance().getTaskManager();
        String titles = wes.stream().map(w -> w.getTitle()).collect(Collectors.joining(", "));
        String description = MpsatUtils.getToolchainDescription(titles);
        manager.queue(task, description, monitor);
    }

}