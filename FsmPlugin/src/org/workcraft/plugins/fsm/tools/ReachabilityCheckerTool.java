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

public class ReachabilityCheckerTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Check for reachability";
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
		HashSet<State> unreachableStates = checkReachability(fsm);
		if (unreachableStates.isEmpty()) {
			JOptionPane.showMessageDialog(null,	"The model does not have unreachable states." ,
					"Verification result", JOptionPane.INFORMATION_MESSAGE);
		} else {
			String stateStr = FsmUtils.statesToString(fsm, unreachableStates);
			JOptionPane.showMessageDialog(null,	"The model has unreachable states:\n" + stateStr,
					"Verification result", JOptionPane.WARNING_MESSAGE);
		}
	}

	private HashSet<State> checkReachability(final Fsm fsm) {
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
				State nextState = (State)curEvent.getSecond();
				if (nextState != null) {
					queue.add(nextState);
				}
			}
		}

		HashSet<State> unreachableStates = new HashSet<State>(fsm.getStates());
		unreachableStates.removeAll(visited);
		return unreachableStates;
	}

}
