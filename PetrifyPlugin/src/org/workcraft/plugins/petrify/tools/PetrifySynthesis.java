package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.SynthesisTool;
import org.workcraft.plugins.petrify.tasks.SynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class PetrifySynthesis extends SynthesisTool {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, StgModel.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final SynthesisTask task = new SynthesisTask(we, getSynthesisParameter());
        final SynthesisResultHandler monitor = new SynthesisResultHandler(we,
                boxSequentialComponents(), boxCombinationalComponents(), sequentialAssign());

        taskManager.queue(task, "Petrify logic synthesis", monitor);
        return we;
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
