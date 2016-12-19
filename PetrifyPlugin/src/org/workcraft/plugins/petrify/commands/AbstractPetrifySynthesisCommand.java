package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractSynthesisCommand;
import org.workcraft.plugins.petrify.tasks.SynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public abstract class AbstractPetrifySynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final SynthesisTask task = new SynthesisTask(we, getSynthesisParameter());
        final SynthesisResultHandler monitor = new SynthesisResultHandler(we,
                boxSequentialComponents(), boxCombinationalComponents(), sequentialAssign());

        taskManager.queue(task, "Petrify logic synthesis", monitor);
    }

    public boolean boxSequentialComponents() {
        return false;
    }

    public boolean boxCombinationalComponents() {
        return false;
    }

    public boolean sequentialAssign() {
        return false;
    }

    public abstract String[] getSynthesisParameter();

}
