package org.workcraft.plugins.graph.properties;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Symbol;
import org.workcraft.plugins.graph.Vertex;

import java.util.Map;

public class SymbolPropertyDescriptor implements PropertyDescriptor {
    private final Graph dg;
    private final Symbol symbol;

    public SymbolPropertyDescriptor(Graph dg, Symbol symbol) {
        this.dg = dg;
        this.symbol = symbol;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return dg.getName(symbol) + " name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return dg.getName(symbol);
    }

    @Override
    public void setValue(Object value) {
        String name = (String) value;
        Node node = dg.getNodeByReference(name);
        if (node == null) {
            dg.setName(symbol, name);
            for (Vertex vertex: dg.getVertices(symbol)) {
                vertex.sendNotification(new PropertyChangedEvent(vertex, Vertex.PROPERTY_SYMBOL));
            }
        } else if (node instanceof Symbol) {
        } else {
            throw new FormatException("Node '" + name + "' already exists and it is not a symbol.");
        }
    }

}
