package org.workcraft.plugins.pog.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.pog.Pog;
import org.workcraft.plugins.pog.Symbol;
import org.workcraft.plugins.pog.Vertex;

public class SymbolPropertyDescriptor implements PropertyDescriptor {
    private final Pog pog;
    private final Symbol symbol;

    public SymbolPropertyDescriptor(Pog pog, Symbol symbol) {
        this.pog = pog;
        this.symbol = symbol;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return pog.getName(symbol) + " name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isCombinable() {
        return false;
    }

    @Override
    public boolean isTemplatable() {
        return false;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        return pog.getName(symbol);
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        String name = (String) value;
        Node node = pog.getNodeByReference(name);
        if (node == null) {
            pog.setName(symbol, name);
            for (Vertex vertex: pog.getVertices(symbol)) {
                vertex.sendNotification(new PropertyChangedEvent(vertex, Vertex.PROPERTY_SYMBOL));
            }
        } else if (node instanceof Symbol) {
        } else {
            throw new FormatException("Node '" + name + "' already exists and it is not a symbol.");
        }
    }

}
