package org.workcraft.plugins.petrify.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyUntoggle extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Untoggle signal transitions [Petrify]";
    }

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, StgModel.class);
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final TransformationTask task = new TransformationTask(we, "Signal transition untoggle", new String[] {"-untog"});
        final TransformationResultHandler monitor = new TransformationResultHandler(we);
        taskManager.queue(task, "Petrify signal transition untoggle", monitor);
        return we;
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

}
