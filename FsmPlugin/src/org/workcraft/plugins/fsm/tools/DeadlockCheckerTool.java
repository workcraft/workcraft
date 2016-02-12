package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.workcraft.VerificationTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.workspace.WorkspaceEntry;

public class DeadlockCheckerTool extends VerificationTool {

    @Override
    public String getDisplayName() {
        return "Deadlock";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Fsm;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Fsm fsm = (Fsm) we.getModelEntry().getMathModel();
        HashSet<State> deadlockStates = checkDeadlock(fsm);
        if (deadlockStates.isEmpty()) {
            JOptionPane.showMessageDialog(null,    "The model is deadlock-free.",
                    "Verification result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            HashSet<State> finalDeadlockStates = new HashSet<State>();
            for (State state: deadlockStates) {
                if (state.isFinal()) {
                    finalDeadlockStates.add(state);
                }
            }
            deadlockStates.removeAll(finalDeadlockStates);
            String message = "The model has a deadlock.";
            if (!deadlockStates.isEmpty()) {
                String stateStr = FsmUtils.statesToString(fsm, deadlockStates);
                message += "\n\nNon-final deadlock states: \n" + stateStr;
            }
            if (!finalDeadlockStates.isEmpty()) {
                String stateStr = FsmUtils.statesToString(fsm, finalDeadlockStates);
                message += "\n\nFinal deadlockstates: \n" + stateStr;
            }
            JOptionPane.showMessageDialog(null,    message,
                    "Verification result", JOptionPane.WARNING_MESSAGE);
        }
    }

    private HashSet<State> checkDeadlock(final Fsm fsm) {
        HashSet<State> deadlockStates = new HashSet<State>();
        HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

        HashSet<State> visited = new HashSet<State>();
        Queue<State> queue = new LinkedList<State>();

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
