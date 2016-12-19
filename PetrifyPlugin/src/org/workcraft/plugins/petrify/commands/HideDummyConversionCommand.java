package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class HideDummyConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Dummy contraction [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final TransformationTask task = new TransformationTask(we, "Dummy contraction", new String[] {"-hide", ".dummy" });
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final TransformationResultHandler monitor = new TransformationResultHandler(we);
        taskManager.queue(task, "Petrify dummy contraction", monitor);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
