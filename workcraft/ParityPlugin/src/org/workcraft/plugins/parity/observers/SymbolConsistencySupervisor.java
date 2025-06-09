package org.workcraft.plugins.parity.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.parity.Parity;
import org.workcraft.plugins.parity.Symbol;
import org.workcraft.plugins.parity.Player0;
import org.workcraft.plugins.parity.Player1;

import java.util.HashSet;

/**
 * StateSupervisor subclass overridden to deal with vertex symbols belonging to
 * Player 0 or Player 1
 */
public class SymbolConsistencySupervisor extends StateSupervisor {
    private final Parity parity;

    /**
     * Constructor
     * @param parity    Current parity game mathematical model
     */
    public SymbolConsistencySupervisor(Parity parity) {
        this.parity = parity;
    }

    /**
     * Handle changes to Player 0 or Player 1 vertices
     * @param e    current StateEvent causing a change to a component
     */
    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent pce) {
            Object sender = e.getSender();
            if ((sender instanceof Player0)
                    && pce.getPropertyName().equals(Player0.PROPERTY_SYMBOL)) {
                handlePlayer0SymbolChange();
            } else if ((sender instanceof Player1)
                    && pce.getPropertyName().equals(Player1.PROPERTY_SYMBOL)) {
                // Update the collection of symbols on a change of vertex symbol property
                handlePlayer1SymbolChange();
            }
        }
    }

    /**
     * Handle hierarchy events if a vertex owned by Player 0 or Player 1 is
     * deleted. Calls handlePlayerXRemoval.
     * @param e    current HierarchyEvent with respect to vertex deletion
     */
    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        if (!parity.keepUnusedSymbols() && (e instanceof NodesDeletingEvent)) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof Player0) {
                    handlePlayer0Removal((Player0) node);
                } else if (node instanceof Player1) {
                    // Remove unused symbols on vertex deletion
                    handlePlayer1Removal((Player1) node);
                }
            }
        }
    }

    /**
     * Handles the mutation of a Player 0 vertex.
     */
    private void handlePlayer0SymbolChange() {
        HashSet<Symbol> unusedSymbols = new HashSet<>(parity.getSymbols());
        for (Player0 v: parity.getPlayer0()) {
            Symbol symbol = v.getSymbol();
            unusedSymbols.remove(symbol);
        }
        parity.remove(unusedSymbols);
    }

    /**
     * Handles the mutation of a Player 1 vertex.
     */
    private void handlePlayer1SymbolChange() {
        HashSet<Symbol> unusedSymbols = new HashSet<>(parity.getSymbols());
        for (Player1 v: parity.getPlayer1()) {
            Symbol symbol = v.getSymbol();
            unusedSymbols.remove(symbol);
        }
        parity.remove(unusedSymbols);
    }

    /**
     * Helper function called by handleHierarchyEvent to deal with deletion of
     * Player 0 vertices.
     * @param p0    Player 0 vertex to be deleted
     */
    private void handlePlayer0Removal(Player0 p0) {
        Symbol symbol = p0.getSymbol();
        if (symbol != null) {
            boolean symbolIsUnused = true;
            for (Player0 v: parity.getPlayer0()) {
                if ((v != p0) && (v.getSymbol() == symbol)) {
                    symbolIsUnused = false;
                    break;
                }
            }
            if (symbolIsUnused) {
                parity.remove(symbol);
            }
        }
    }

    /**
     * Helper function called by handleHierarchyEvent to deal with deletion of
     * Player 1 vertices.
     * @param p1    Player 1 vertex to be deleted
     */
    private void handlePlayer1Removal(Player1 p1) {
        Symbol symbol = p1.getSymbol();
        if (symbol != null) {
            boolean symbolIsUnused = true;
            for (Player1 v: parity.getPlayer1()) {
                if ((v != p1) && (v.getSymbol() == symbol)) {
                    symbolIsUnused = false;
                    break;
                }
            }
            if (symbolIsUnused) {
                parity.remove(symbol);
            }
        }
    }
}