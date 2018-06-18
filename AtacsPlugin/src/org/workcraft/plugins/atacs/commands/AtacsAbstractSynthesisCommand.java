package org.workcraft.plugins.atacs.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractSynthesisCommand;
import org.workcraft.plugins.atacs.tasks.AtacsSynthesisResultHandler;
import org.workcraft.plugins.atacs.tasks.AtacsSynthesisTask;
import org.workcraft.plugins.stg.*;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.HashSet;
import java.util.LinkedList;

public abstract class AtacsAbstractSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueSynthesis(we);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        WorkspaceEntry result = null;
        AtacsSynthesisResultHandler monitor = queueSynthesis(we);
        if (monitor != null) {
            result = monitor.waitForHandledResult();
        }
        return result;
    }

    private AtacsSynthesisResultHandler queueSynthesis(WorkspaceEntry we) {
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
        AtacsSynthesisTask task = new AtacsSynthesisTask(we, getSynthesisParameter(), mutexes);
        AtacsSynthesisResultHandler monitor = new AtacsSynthesisResultHandler(we,
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
