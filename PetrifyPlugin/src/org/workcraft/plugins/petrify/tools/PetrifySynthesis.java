package org.workcraft.plugins.petrify.tools;

import org.workcraft.Framework;
import org.workcraft.SynthesisTool;
import org.workcraft.plugins.petrify.SynthesisResultHandler;
import org.workcraft.plugins.petrify.tasks.SynthesisTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class PetrifySynthesis extends SynthesisTool {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, STGModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        SynthesisTask task = new SynthesisTask(we, getSynthesisParameter());
        final Framework framework = Framework.getInstance();
        SynthesisResultHandler monitor = new SynthesisResultHandler(we, boxSequentialComponents(), boxCombinationalComponents());
        framework.getTaskManager().queue(task, "Petrify logic synthesis", monitor);
    }

    public boolean boxSequentialComponents() {
        return false;
    }

    public boolean boxCombinationalComponents() {
        return false;
    }

    public abstract String[] getSynthesisParameter();

}
