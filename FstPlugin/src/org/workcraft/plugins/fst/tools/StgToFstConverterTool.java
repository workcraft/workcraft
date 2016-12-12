package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fst.task.StgToFstConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConverterTool extends ConversionTool {

    public boolean isBinary() {
        return false;
    }

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        if (isBinary()) {
            return "Finate State Transducer (binary-encoded) [Petrify]";
        } else {
            return "Finate State Transducer (basic) [Petrify]";
        }
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, Stg.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final WriteSgConversionTask task = new WriteSgConversionTask(we, isBinary());
        final StgToFstConversionResultHandler monitor = new StgToFstConversionResultHandler(task);
        taskManager.queue(task, "Building state graph", monitor);
        return we;
    }

}
