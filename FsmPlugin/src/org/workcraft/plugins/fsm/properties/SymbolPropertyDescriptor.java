package org.workcraft.plugins.fsm.properties;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.Symbol;

import java.util.Map;

public class SymbolPropertyDescriptor implements PropertyDescriptor {
    private final Fsm fsm;
    private final Symbol symbol;

    public SymbolPropertyDescriptor(Fsm fsm, Symbol symbol) {
        this.fsm = fsm;
        this.symbol = symbol;
    }

    @Override
    public Map<Object, String> getChoice() {
        return null;
    }

    @Override
    public String getName() {
        return fsm.getName(symbol) + " name";
    }

    @Override
    public Class<?> getType() {
        return String.class;
    }

    @Override
    public Object getValue() {
        return fsm.getName(symbol);
    }

    @Override
    public void setValue(Object value) {
        String name = (String) value;
        Node node = fsm.getNodeByReference(name);
        if (node == null) {
            fsm.setName(symbol, name);
            for (Event event: fsm.getEvents(symbol)) {
                event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
            }
        } else if (node instanceof Symbol) {
        } else {
            throw new FormatException("Node '" + name + "' already exists and it is not a symbol.");
        }
    }

}
