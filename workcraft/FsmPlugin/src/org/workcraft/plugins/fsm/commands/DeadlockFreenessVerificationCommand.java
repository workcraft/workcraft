package org.workcraft.plugins.fsm.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.utils.FsmUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class DeadlockFreenessVerificationCommand extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Deadlock freeness";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Fsm.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        execute(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!isApplicableTo(we)) {
            return null;
        }
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final Fsm fsm = WorkspaceUtils.getAs(we, Fsm.class);
        HashSet<State> deadlockStates = checkDeadlock(fsm);
        if (deadlockStates.isEmpty()) {
            DialogUtils.showInfo("The model is deadlock-free.", TITLE);
        } else {
            HashSet<State> finalDeadlockStates = new HashSet<>();
            for (State state: deadlockStates) {
                if (state.isFinal()) {
                    finalDeadlockStates.add(state);
                }
            }
            deadlockStates.removeAll(finalDeadlockStates);
            String message = "The model has a deadlock.";
            if (!deadlockStates.isEmpty()) {
                String stateStr = ReferenceHelper.getNodesAsWrapString(fsm, deadlockStates);
                message += "\n\nNon-final deadlock states: \n" + stateStr;
            }
            if (!finalDeadlockStates.isEmpty()) {
                String stateStr = ReferenceHelper.getNodesAsWrapString(fsm, finalDeadlockStates);
                message += "\n\nFinal deadlock states: \n" + stateStr;
            }
            message += "\n\nSelect deadlock states?\n";
            if (DialogUtils.showConfirmInfo(message, TITLE, true)) {
                VisualFsm visualFsm = WorkspaceUtils.getAs(we, VisualFsm.class);
                mainWindow.getToolbox(we).selectToolInstance(SelectionTool.class);
                SelectionHelper.selectByReferencedComponents(visualFsm, deadlockStates);
            }
        }
        return deadlockStates.isEmpty();
    }

    private HashSet<State> checkDeadlock(final Fsm fsm) {
        HashSet<State> deadlockStates = new HashSet<>();
        HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

        HashSet<State> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>();

        State initialState = fsm.getInitialState();
        if (initialState != null) {
            queue.add(initialState);
        }

        while (!queue.isEmpty()) {
            State curState = queue.remove();
            if (visited.contains(curState)) continue;
            visited.add(curState);
            for (Event curEvent: stateEvents.get(curState)) {
                State nextState = (State) curEvent.getSecond();
                if (nextState != null) {
                    queue.add(nextState);
                }
            }
            if (stateEvents.get(curState).isEmpty()) {
                deadlockStates.add(curState);
            }
        }
        return deadlockStates;
    }

}
