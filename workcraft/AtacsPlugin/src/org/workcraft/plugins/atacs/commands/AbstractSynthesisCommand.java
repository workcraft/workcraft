package org.workcraft.plugins.atacs.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.atacs.AtacsSettings;
import org.workcraft.plugins.atacs.tasks.AtacsTask;
import org.workcraft.plugins.atacs.tasks.SynthesisResultHandlingMonitor;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractSynthesisCommand extends org.workcraft.commands.AbstractSynthesisCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public boolean isVisibleInMenu() {
        return AtacsSettings.getShowInMenu();
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueSynthesis(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        WorkspaceEntry result = null;
        SynthesisResultHandlingMonitor monitor = queueSynthesis(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private SynthesisResultHandlingMonitor queueSynthesis(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Collection<String> signalRefs = StgUtils.getSignalsWithToggleTransitions(stg);
        if (!signalRefs.isEmpty()) {
            DialogUtils.showError(TextUtils.wrapMessageWithItems(
                    "ATACS cannot synthesise STGs with toggle transitions. Problematic signal",
                    signalRefs));

            return null;
        }
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        AtacsTask task = new AtacsTask(we, getSynthesisParameter(), mutexes);
        SynthesisResultHandlingMonitor monitor = new SynthesisResultHandlingMonitor(we,
                boxSequentialComponents(), boxCombinationalComponents(),
                celementAssign(), sequentialAssign(), mutexes);

        taskManager.queue(task, "ATACS logic synthesis", monitor);
        return monitor;
    }

    public boolean boxSequentialComponents() {
        return false;
    }

    public boolean boxCombinationalComponents() {
        return false;
    }

    public boolean celementAssign() {
        return false;
    }

    public boolean sequentialAssign() {
        return false;
    }

    public abstract List<String> getSynthesisParameter();

}
