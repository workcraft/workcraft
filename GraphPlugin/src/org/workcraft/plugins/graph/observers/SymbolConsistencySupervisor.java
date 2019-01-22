package org.workcraft.plugins.graph.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Symbol;
import org.workcraft.plugins.graph.Vertex;

import java.util.HashSet;

public class SymbolConsistencySupervisor extends StateSupervisor {
    private final Graph graph;

    public SymbolConsistencySupervisor(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            if ((sender instanceof Vertex) && pce.getPropertyName().equals(Vertex.PROPERTY_SYMBOL)) {
                // Update the collection of symbols on a change of vertex symbol property
                handleVertexSymbolChange((Vertex) sender);
            }
        }
    }

    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        if (!graph.keepUnusedSymbols() && (e instanceof NodesDeletingEvent)) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof Vertex) {
                    // Remove unused symbols on vertex deletion
                    handleVertexRemoval((Vertex) node);
                }
            }
        }
    }

    private void handleVertexSymbolChange(Vertex vertex) {
        HashSet<Symbol> unusedSymbols = new HashSet<>(graph.getSymbols());
        for (Vertex v: graph.getVertices()) {
            Symbol symbol = v.getSymbol();
            unusedSymbols.remove(symbol);
        }
        graph.remove(unusedSymbols);
    }

    private void handleVertexRemoval(Vertex vertex) {
        Symbol symbol = vertex.getSymbol();
        if (symbol != null) {
            boolean symbolIsUnused = true;
            for (Vertex v: graph.getVertices()) {
                if ((v != vertex) && (v.getSymbol() == symbol)) {
                    symbolIsUnused = false;
                    break;
                }
            }
            if (symbolIsUnused) {
                graph.remove(symbol);
            }
        }
    }

}
