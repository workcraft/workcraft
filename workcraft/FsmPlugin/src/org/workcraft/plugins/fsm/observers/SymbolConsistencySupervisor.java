package org.workcraft.plugins.fsm.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.Symbol;

import java.util.HashSet;

public class SymbolConsistencySupervisor extends StateSupervisor {

    private final Fsm fsm;

    public SymbolConsistencySupervisor(Fsm fsm) {
        this.fsm = fsm;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent pce) {
            Object sender = e.getSender();
            if ((sender instanceof Event) && pce.getPropertyName().equals(Event.PROPERTY_SYMBOL)) {
                // Update the collection of symbols on a change of event symbol property
                handleEventSymbolChange();
            }
        }
    }

    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof Event) {
                    // Remove unused symbols on event deletion
                    handleEventRemoval((Event) node);
                }
            }
        }
    }

    private void handleEventSymbolChange() {
        HashSet<Symbol> unusedSymbols = new HashSet<>(fsm.getSymbols());
        for (Event e: fsm.getEvents()) {
            Symbol symbol = e.getSymbol();
            unusedSymbols.remove(symbol);
        }
        fsm.remove(unusedSymbols);
    }

    private void handleEventRemoval(Event event) {
        Symbol symbol = event.getSymbol();
        if (symbol != null) {
            boolean symbolIsUnused = true;
            for (Event e: fsm.getEvents()) {
                if ((e != event) && (e.getSymbol() == symbol)) {
                    symbolIsUnused = false;
                    break;
                }
            }
            if (symbolIsUnused) {
                fsm.remove(symbol);
            }
        }
    }

}
