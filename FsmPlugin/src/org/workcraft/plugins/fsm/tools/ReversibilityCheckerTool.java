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

public class ReversibilityCheckerTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Reversibility";
	}

	@Override
	public String getSection() {
		return "! Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Fsm;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Fsm fsm = (Fsm)we.getModelEntry().getMathModel();
		HashSet<State> irreversibleStates = checkReversibility(fsm);
		if (irreversibleStates.isEmpty()) {
			JOptionPane.showMessageDialog(null,	"The model is reversible." ,
					"Verification result", JOptionPane.INFORMATION_MESSAGE);
		} else {
			String stateStr = FsmUtils.statesToString(fsm, irreversibleStates);
			JOptionPane.showMessageDialog(null,	"The model has irreversible states:\n" + stateStr,
					"Verification result", JOptionPane.WARNING_MESSAGE);
		}
	}

	private HashSet<State> checkReversibility(final Fsm fsm) {

		State initialState = fsm.getInitialState();
		HashSet<State> forwardStates = getForwardReachableStates(fsm, initialState);
		HashSet<State> backwardStates = getBackwardReachableStates(fsm, initialState);

		HashSet<State> ireversibleStates = new HashSet<State>(forwardStates);
		ireversibleStates.removeAll(backwardStates);
		return ireversibleStates;
	}

	private HashSet<State> getForwardReachableStates(final Fsm fsm, State initialState) {
		HashMap<State, HashSet<Event>> stateSuccEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);

		HashSet<State> visitedStates = new HashSet<State>();
		Queue<State> queueStates = new LinkedList<State>();

		if (initialState != null) {
			queueStates.add(initialState);
		}

		while (!queueStates.isEmpty()) {
			State curState = queueStates.remove();
			if (visitedStates.contains(curState)) continue;
			visitedStates.add(curState);
			for (Event curEvent: stateSuccEvents.get(curState)) {
				State nextState = (State)curEvent.getSecond();
				if (nextState != null) {
					queueStates.add(nextState);
				}
			}
		}
		return visitedStates;
	}

	private HashSet<State> getBackwardReachableStates(final Fsm fsm, State initialState) {
		HashMap<State, HashSet<Event>> statePrevEvents = FsmUtils.calcStateIncommingEventsMap(fsm);

		HashSet<State> visitedStates = new HashSet<State>();
		Queue<State> queueStates = new LinkedList<State>();

		if (initialState != null) {
			queueStates.add(initialState);
		}

		while (!queueStates.isEmpty()) {
			State curState = queueStates.remove();
			if (visitedStates.contains(curState)) continue;
			visitedStates.add(curState);
			for (Event curEvent: statePrevEvents.get(curState)) {
				State prevState = (State)curEvent.getFirst();
				if (prevState != null) {
					queueStates.add(prevState);
				}
			}
		}
		return visitedStates;
	}

}
