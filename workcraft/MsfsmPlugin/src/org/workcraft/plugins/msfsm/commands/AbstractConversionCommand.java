package org.workcraft.plugins.msfsm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.MenuOrdering;
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

public abstract class AbstractConversionCommand implements ScriptableCommand<Collection<WorkspaceEntry>>, MenuOrdering  {

    @Override
    public final String getSection() {
        return org.workcraft.commands.AbstractConversionCommand.SECTION_TITLE;
    }

    @Override
    public int getPriority() {
        return 0;
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
    public boolean isVisibleInMenu() {
        return MsfsmSettings.getShowInMenu();
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueConversion(we);
    }

    @Override
    public Collection<WorkspaceEntry> execute(WorkspaceEntry we) {
        Collection<WorkspaceEntry> result = null;
        ConversionResultHandlingMonitor monitor = queueConversion(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private ConversionResultHandlingMonitor queueConversion(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        ConversionTask task = new ConversionTask(we, getFileName(), getConversionCommands());
        boolean hasSignals = hasSignals(we);
        ConversionResultHandlingMonitor monitor = new ConversionResultHandlingMonitor(we, !hasSignals);
        taskManager.queue(task, "MSFSM conversion", monitor);
        return monitor;
    }

    public boolean hasSignals(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class) || WorkspaceUtils.isApplicable(we, Fst.class);
    }

    public abstract String getFileName();

    public abstract String[] getConversionCommands();

}
