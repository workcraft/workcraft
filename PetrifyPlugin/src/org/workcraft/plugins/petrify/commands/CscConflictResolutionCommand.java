package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CscConflictResolutionCommand implements Command {

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
    public void run(WorkspaceEntry we) {
        final TransformationTask task = new TransformationTask(we, "CSC conflicts resolution", new String[] {"-csc"});
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final TransformationResultHandler monitor = new TransformationResultHandler(we);
        taskManager.queue(task, "Petrify CSC conflicts resolution", monitor);
    }

}
