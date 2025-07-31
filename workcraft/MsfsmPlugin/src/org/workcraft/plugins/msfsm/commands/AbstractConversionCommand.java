package org.workcraft.plugins.msfsm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.msfsm.MsfsmSettings;
import org.workcraft.plugins.msfsm.tasks.ConversionResultHandlingMonitor;
import org.workcraft.plugins.msfsm.tasks.ConversionTask;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public abstract class AbstractConversionCommand implements ScriptableCommand<Collection<WorkspaceEntry>> {

    @Override
    public final Category getCategory() {
        return org.workcraft.commands.AbstractConversionCommand.CATEGORY;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public Visibility getVisibility() {
        return MsfsmSettings.getShowInMenu() ? Visibility.APPLICABLE : Visibility.NEVER;
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueConversion(we);
    }

    @Override
    public Collection<WorkspaceEntry> execute(WorkspaceEntry we) {
        ConversionResultHandlingMonitor monitor = queueConversion(we);
        return monitor.waitForHandledResult();
    }

    private ConversionResultHandlingMonitor queueConversion(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        ConversionTask task = new ConversionTask(we, getFileName(), getConversionCommands());
        boolean hasSignals = hasSignals(we);
        ConversionResultHandlingMonitor monitor = new ConversionResultHandlingMonitor(!hasSignals);
        taskManager.queue(task, "MSFSM conversion", monitor);
        return monitor;
    }

    public boolean hasSignals(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class) || WorkspaceUtils.isApplicable(we, Fst.class);
    }

    public abstract String getFileName();

    public abstract String[] getConversionCommands();

}
