package org.workcraft.plugins.fst.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fst.task.PetriToFsmConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetriToFsmConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Finite State Machine [Petrify]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNet.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final WriteSgConversionTask task = new WriteSgConversionTask(we, false);
        final PetriToFsmConversionResultHandler monitor = new PetriToFsmConversionResultHandler(task);
        taskManager.execute(task, "Building state graph", monitor);
        return monitor.getResult();
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
