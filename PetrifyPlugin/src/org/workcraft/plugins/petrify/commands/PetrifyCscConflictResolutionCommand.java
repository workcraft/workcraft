package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.ScriptableCommand;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.PetrifyTransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifyCscConflictResolutionCommand implements ScriptableCommand {

    @Override
    public String getSection() {
        return "Encoding conflicts";
    }

    @Override
    public String getDisplayName() {
        return "Resolve CSC conflicts [Petrify]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final PetrifyTransformationTask task = new PetrifyTransformationTask(we,
                "CSC conflicts resolution", new String[] {"-csc"});

        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we);
        taskManager.execute(task, "Petrify CSC conflicts resolution", monitor);
        return monitor.getResult();
    }

    @Override
    public void run(WorkspaceEntry we) {
        final PetrifyTransformationTask task = new PetrifyTransformationTask(we,
                "CSC conflicts resolution", new String[] {"-csc"});

        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final PetrifyTransformationResultHandler monitor = new PetrifyTransformationResultHandler(we);
        taskManager.queue(task, "Petrify CSC conflicts resolution", monitor);
    }

}
