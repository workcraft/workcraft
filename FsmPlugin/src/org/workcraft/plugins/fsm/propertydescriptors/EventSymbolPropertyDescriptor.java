package org.workcraft.plugins.fsm.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.Symbol;

public class EventSymbolPropertyDescriptor implements PropertyDescriptor {
    private final Fsm fsm;
    private final Event event;

    public EventSymbolPropertyDescriptor(Fsm fsm, Event event) {
        this.fsm = fsm;
        this.event = event;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return Event.PROPERTY_SYMBOL;
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() throws InvocationTargetException {
        Symbol symbol = event.getSymbol();
        String symbolName = "";
        if (symbol != null) {
            symbolName = fsm.getName(symbol);
        }
        return symbolName;
    }

    @Override
    public void setValue(Object value) throws InvocationTargetException {
        Symbol symbol = null;
        String symbolName = (String)value;
        if (!symbolName.isEmpty()) {
            Node node = fsm.getNodeByReference(symbolName);
            if (node instanceof Symbol) {
                symbol = (Symbol)node;
            } else {
                symbol = fsm.createSymbol(symbolName);
            }
        }
        event.setSymbol(symbol);
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
