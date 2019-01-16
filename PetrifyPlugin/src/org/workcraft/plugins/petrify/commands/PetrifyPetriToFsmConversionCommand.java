package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petrify.tasks.PetriToFsmConversionResultHandler;
import org.workcraft.plugins.petrify.tasks.WriteSgConversionTask;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetrifyPetriToFsmConversionCommand extends AbstractConversionCommand {

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
        if (Hierarchy.isHierarchical(we.getModelEntry())) {
            DialogUtils.showError("Finite State Machine cannot be derived from a hierarchical Petri Net.",
                    "Conversion error");
            return null;
        }
        final TaskManager taskManager = framework.getTaskManager();
        final WriteSgConversionTask task = new WriteSgConversionTask(we, false);
        final PetriToFsmConversionResultHandler monitor = new PetriToFsmConversionResultHandler(task);
        taskManager.execute(task, "Building state graph", monitor);
        return monitor.waitForHandledResult();
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
