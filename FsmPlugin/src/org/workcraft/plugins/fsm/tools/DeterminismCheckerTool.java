package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Tool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
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
			String stateStr = FsmUtils.statesToString(fsm, nondeterministicStates);
			JOptionPane.showMessageDialog(null,	"FSM has non-deterministic states:\n" + stateStr,
					"Verification result", JOptionPane.WARNING_MESSAGE);
		}
	}

	private HashSet<State> checkDeterminism(final Fsm fsm) {
		HashSet<State> nondeterministicStates = new HashSet<State>();
		HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateEventsMap(fsm);
		for (State state: stateEvents.keySet()) {
			HashSet<Symbol> symbols = new HashSet<Symbol>();
			for (Event event: stateEvents.get(state)) {
				Symbol symbol = event.getSymbol();
				boolean symbolIsDummy = fsm.getName(symbol).isEmpty();
				if (symbolIsDummy || symbols.contains(symbol)) {
					nondeterministicStates.add(state);
				} else {
					symbols.add(symbol);
				}
			}
		}
		return nondeterministicStates;
	}

}
