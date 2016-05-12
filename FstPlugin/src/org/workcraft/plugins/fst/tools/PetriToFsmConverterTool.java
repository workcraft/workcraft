package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fst.task.PetriToFsmConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToFsmConverterTool extends ConversionTool {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, PetriNet.class);
    }

    @Override
    public String getDisplayName() {
        return "Finite State Machine [Petrify]";
    }

    @Override
    public void run(WorkspaceEntry we) {
        WriteSgConversionTask task = new WriteSgConversionTask(we, false);
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(task, "Building state graph", new PetriToFsmConversionResultHandler(task));
    }

}
