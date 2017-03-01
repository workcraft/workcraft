package org.workcraft.plugins.wtg.commands;

import org.workcraft.Framework;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.plugins.wtg.tasks.WaverConversionTask;
import org.workcraft.plugins.wtg.tasks.WtgToStgConversionResultHandler;
import org.workcraft.tasks.TaskManager;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class WtgToStgConversionCommand extends AbstractConversionCommand {

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph [Waver]";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Wtg.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final WaverConversionTask task = new WaverConversionTask(we);
        final WtgToStgConversionResultHandler monitor = new WtgToStgConversionResultHandler(task);
        taskManager.execute(task, "Converting to STG", monitor);
        return monitor.getResult();
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
