package org.workcraft.plugins.fst.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.task.StgToFstConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgToFstConversionCommand extends AbstractConversionCommand {

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
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final WriteSgConversionTask task = new WriteSgConversionTask(we, isBinary());
        final StgToFstConversionResultHandler monitor = new StgToFstConversionResultHandler(task);
        taskManager.queue(task, "Building state graph", monitor);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
