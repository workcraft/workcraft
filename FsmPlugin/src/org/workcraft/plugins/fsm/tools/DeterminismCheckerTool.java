package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Tool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.workspace.WorkspaceEntry;

public class DeterminismCheckerTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Check for determinism";
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
		HashSet<State> nondeterministicStates = checkDeterminism(fsm);
		if (nondeterministicStates.isEmpty()) {
			JOptionPane.showMessageDialog(null,	"FSM is deterministic." ,
					"Verification result", JOptionPane.INFORMATION_MESSAGE);
		} else {
			String stateStr = statesToString(fsm, nondeterministicStates);
			JOptionPane.showMessageDialog(null,	"FSM has non-deterministic states: \n {" + stateStr + "}",
					"Verification result", JOptionPane.WARNING_MESSAGE);
		}
	}

	private String statesToString(final Fsm fsm, HashSet<State> states) {
		String result = "";
		for (State state: states) {
			if (!result.isEmpty()) {
				result += ", ";
			}
			result += fsm.getNodeReference(state);
		}
		return result;
	}

	private HashSet<State> checkDeterminism(final Fsm fsm) {
		HashSet<State> nondeterministicStates = new HashSet<State>();
		HashMap<State, HashSet<Event>> stateEvents = calcStateEventsMap(fsm);
		for (State state: stateEvents.keySet()) {
			HashSet<String> symbols = new HashSet<String>();
			for (Event event: stateEvents.get(state)) {
				String symbol = event.getSymbol();
				if (symbol.isEmpty() || symbols.contains(symbol)) {
					nondeterministicStates.add(state);
				} else {
					symbols.add(symbol);
				}
			}
		}
		return nondeterministicStates;
	}


	private HashMap<State, HashSet<Event>> calcStateEventsMap(final Fsm fsm) {
		HashMap<State, HashSet<Event>> stateEvents = new HashMap<State, HashSet<Event>>();
		for (State state: fsm.getStates()) {
			HashSet<Event> events = new HashSet<Event>();
			stateEvents.put(state, events);
		}
		for (Event event: fsm.getEvents()) {
			State state = (State)event.getFirst();
			HashSet<Event> events = stateEvents.get(state);
			events.add(event);
		}
		return stateEvents;
	}

}
