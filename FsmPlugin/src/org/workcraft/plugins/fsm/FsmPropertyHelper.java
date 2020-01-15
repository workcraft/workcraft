package org.workcraft.plugins.fsm;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.observation.PropertyChangedEvent;

import java.util.Collection;

public class FsmPropertyHelper {

    public static PropertyDescriptor getSymbolProperty(VisualFsm fsm, Symbol symbol) {
        String className = symbol.getClass().getSimpleName();
        String symbolName = fsm.getMathName(symbol);

        return new PropertyDeclaration<>(TextAction.class, className + " " + symbolName,
                value -> {
                    String newName = value.getText();
                    Fsm mathFsm = fsm.getMathModel();
                    Node node = mathFsm.getNodeByReference(newName);
                    Collection<Event> events = mathFsm.getEvents(symbol);
                    if (node == null) {
                        fsm.setMathName(symbol, newName);
                    } else if (node instanceof Symbol) {
                        Symbol existingSymbol = (Symbol) node;
                        for (Event event : events) {
                            event.setSymbol(existingSymbol);
                        }
                    } else {
                        throw new FormatException("Node '" + value + "' already exists and it is not a " + className + ".");
                    }
                    for (Event event : events) {
                        event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
                    }
                },
                () -> new TextAction(symbolName, new Action(PropertyHelper.SEARCH_SYMBOL,
                        () -> {
                            fsm.selectNone();
                            fsm.addToSelection(fsm.getVisualEvents(symbol));
                        }, "Select all events of " + className + " '" + symbolName + "'")
                ));
    }

    public static PropertyDescriptor getEventSymbolProperty(Fsm fsm, Event event) {
        return new PropertyDeclaration<>(String.class, Event.PROPERTY_SYMBOL,
                value -> {
                    Symbol symbol = null;
                    if (!value.isEmpty()) {
                        Node node = fsm.getNodeByReference(value);
                        if (node instanceof Symbol) {
                            symbol = (Symbol) node;
                        } else {
                            symbol = fsm.createSymbol(value);
                        }
                    }
                    event.setSymbol(symbol);
                },
                () -> {
                    Symbol symbol = event.getSymbol();
                    String symbolName = "";
                    if (symbol != null) {
                        symbolName = fsm.getName(symbol);
                    }
                    return symbolName;
                })
                .setCombinable().setTemplatable();
    }

}
