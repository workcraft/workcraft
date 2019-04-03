package org.workcraft.plugins.atacs.commands;

import org.workcraft.Framework;
import org.workcraft.plugins.atacs.AtacsSettings;
import org.workcraft.plugins.atacs.tasks.SynthesisResultHandler;
import org.workcraft.plugins.atacs.tasks.SynthesisTask;
import org.workcraft.plugins.stg.*;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.HashSet;
import java.util.LinkedList;

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
        SynthesisResultHandler monitor = queueSynthesis(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private SynthesisResultHandler queueSynthesis(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        HashSet<String> signalRefs = getSignalsWithToggleTransitions(stg);
        if (!signalRefs.isEmpty()) {
            DialogUtils.showError("ATACS cannot synthesise STGs with toggle transitions. Problematic signals: "
                    + String.join(", ", signalRefs));
            return null;
        }
        LinkedList<Mutex> mutexes = MutexUtils.getImplementableMutexes(stg);
        if (mutexes == null) {
            return null;
        }
        MutexUtils.logInfoPossiblyImplementableMutex(mutexes);
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        SynthesisTask task = new SynthesisTask(we, getSynthesisParameter(), mutexes);
        SynthesisResultHandler monitor = new SynthesisResultHandler(we,
                boxSequentialComponents(), boxCombinationalComponents(), sequentialAssign(),
                mutexes);

        taskManager.queue(task, "ATACS logic synthesis", monitor);
        return monitor;
    }

    private HashSet<String> getSignalsWithToggleTransitions(Stg stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition st: stg.getSignalTransitions()) {
            if (st.getDirection() == SignalTransition.Direction.TOGGLE) {
                String signalRef = stg.getSignalReference(st);
                result.add(signalRef);
            }
        }
        return result;
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
