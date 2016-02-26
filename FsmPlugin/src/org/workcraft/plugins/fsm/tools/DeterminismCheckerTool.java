package org.workcraft.plugins.fsm.tools;

import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.VerificationTool;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.fsm.Symbol;
import org.workcraft.workspace.WorkspaceEntry;

public class DeterminismCheckerTool extends VerificationTool {

    @Override
    public String getDisplayName() {
        return "Determinism";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Fsm;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Fsm fsm = (Fsm) we.getModelEntry().getMathModel();
        HashSet<State> nondeterministicStates = checkDeterminism(fsm);
        if (nondeterministicStates.isEmpty()) {
            JOptionPane.showMessageDialog(null,    "The model is deterministic.",
                    "Verification result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String stateStr = FsmUtils.statesToString(fsm, nondeterministicStates);
            JOptionPane.showMessageDialog(null,    "The model has non-deterministic states:\n" + stateStr,
                    "Verification result", JOptionPane.WARNING_MESSAGE);
        }
    }

    private HashSet<State> checkDeterminism(final Fsm fsm) {
        HashSet<State> nondeterministicStates = new HashSet<>();
        HashMap<State, HashSet<Event>> stateEvents = FsmUtils.calcStateOutgoingEventsMap(fsm);
        for (State state: stateEvents.keySet()) {
            HashSet<Symbol> symbols = new HashSet<>();
            for (Event event: stateEvents.get(state)) {
                Symbol symbol = event.getSymbol();
                if (!fsm.isDeterministicSymbol(symbol) || symbols.contains(symbol)) {
                    nondeterministicStates.add(state);
                } else {
                    symbols.add(symbol);
                }
            }
        }
        return nondeterministicStates;
    }

}
