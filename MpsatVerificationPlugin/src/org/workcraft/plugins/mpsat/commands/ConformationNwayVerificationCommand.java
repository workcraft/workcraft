package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat.gui.NwayDialog;
import org.workcraft.plugins.mpsat.tasks.ConformationNwayTask;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
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

public class ConformationNwayVerificationCommand extends AbstractVerificationCommand {

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
        queueVerification();
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandler monitor = queueVerification();
        Result<? extends VerificationChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private VerificationChainResultHandler queueVerification() {
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
            if (!dialog.reveal()) {
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

        ConformationNwayTask task = new ConformationNwayTask(wes);
        TaskManager manager = framework.getTaskManager();
        String titles = wes.stream().map(w -> w.getTitle()).collect(Collectors.joining(", "));
        String description = MpsatUtils.getToolchainDescription(titles);
        VerificationChainResultHandler monitor = new VerificationChainResultHandler(wes);
        manager.queue(task, description, monitor);
        return monitor;
    }

}