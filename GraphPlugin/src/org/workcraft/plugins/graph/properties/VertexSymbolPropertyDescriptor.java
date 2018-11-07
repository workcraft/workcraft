package org.workcraft.plugins.graph.properties;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Symbol;
import org.workcraft.plugins.graph.Vertex;

import java.util.Map;

public class VertexSymbolPropertyDescriptor implements PropertyDescriptor {
    private final Graph graph;
    private final Vertex vertex;

    public VertexSymbolPropertyDescriptor(Graph graph, Vertex vertex) {
        this.graph = graph;
        this.vertex = vertex;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return Vertex.PROPERTY_SYMBOL;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        Symbol symbol = vertex.getSymbol();
        String symbolName = "";
        if (symbol != null) {
            symbolName = graph.getName(symbol);
        }
        return symbolName;
    }

    @Override
    public void setValue(Object value) {
        Symbol symbol = null;
        String symbolName = (String) value;
        if (!symbolName.isEmpty()) {
            Node node = graph.getNodeByReference(symbolName);
            if (node instanceof Symbol) {
                symbol = (Symbol) node;
            } else {
                symbol = graph.createSymbol(symbolName);
            }
        }
        vertex.setSymbol(symbol);
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return true;
    }

    @Override
    public boolean isTemplatable() {
        return true;
    }
}
