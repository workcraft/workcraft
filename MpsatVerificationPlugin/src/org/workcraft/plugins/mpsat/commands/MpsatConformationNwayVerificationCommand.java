package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat.gui.NwayDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatConformationNwayTask;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MpsatConformationNwayVerificationCommand extends AbstractVerificationCommand {

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
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        List<WorkspaceEntry> wes = new ArrayList<>();
        if (!framework.isInGuiMode()) {
            for (WorkspaceEntry work: workspace.getWorks()) {
                if (WorkspaceUtils.isApplicable(work, StgModel.class)) {
                    wes.add(work);
                }
            }
        } else {
            MainWindow mainWindow = framework.getMainWindow();
            NwayDialog dialog = new NwayDialog(mainWindow);
            dialog.checkAll();
            GuiUtils.centerToParent(dialog, mainWindow);
            if (!dialog.run()) {
                return null;
            }

            Set<Path<String>> paths = dialog.getSourcePaths();
            if (paths != null) {
                for (Path<String> path : paths) {
                    wes.add(workspace.getWork(path));
                }
            }
        }

        if (wes.size() < 2) {
            DialogUtils.showWarning("At least two STGs are required for N-way conformation.");
            return null;
        }

        MpsatConformationNwayTask task = new MpsatConformationNwayTask(wes);
        TaskManager manager = framework.getTaskManager();
        String titles = wes.stream().map(w -> w.getTitle()).collect(Collectors.joining(", "));
        String description = MpsatUtils.getToolchainDescription(titles);
        MpsatChainResultHandler monitor = new MpsatChainResultHandler(wes);
        manager.queue(task, description, monitor);
        return monitor;
    }

}