package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JOptionPane;

import org.workcraft.Tool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.workspace.WorkspaceEntry;

public class DeadlockCheckerTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Check for deadlock";
	}

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Fsm;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Fsm fsm = (Fsm)we.getModelEntry().getMathModel();
		HashSet<State> deadlockStates = checkDeadlock(fsm);
		if (deadlockStates.isEmpty()) {
			JOptionPane.showMessageDialog(null,	"FSM is deadlock-free." ,
					"Verification result", JOptionPane.INFORMATION_MESSAGE);
		} else {
			String stateStr = FsmUtils.statesToString(fsm, deadlockStates);
			JOptionPane.showMessageDialog(null,	"FSM has deadlock states:\n" + stateStr,
					"Verification result", JOptionPane.WARNING_MESSAGE);
		}
	}

	private HashSet<State> checkDeadlock(final Fsm fsm) {
		HashSet<State> deadlockStates = new HashSet<State>();
		HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateEventsMap(fsm);

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
				State nextState = (State)curEvent.getSecond();
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
