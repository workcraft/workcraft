package org.workcraft.plugins.fsm;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.FormatException;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.properties.TextAction;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.utils.SortUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FsmPropertyHelper {

    public static Collection<PropertyDescriptor> getSymbolProperties(VisualFsm fsm) {
        Collection<PropertyDescriptor> result = new ArrayList<>();
        List<Symbol> symbols = new ArrayList<>(fsm.getMathModel().getSymbols());
        symbols.sort((s1, s2) -> SortUtils.compareNatural(fsm.getMathReference(s1), fsm.getMathReference(s2)));
        for (Symbol symbol : symbols) {
            result.add(getSymbolProperty(fsm, symbol));
        }
        return result;
    }

    private static PropertyDescriptor getSymbolProperty(VisualFsm fsm, Symbol symbol) {
        String symbolName = fsm.getMathName(symbol);
        Action rightAction = new Action(PropertyHelper.SEARCH_SYMBOL,
                () -> {
                    fsm.selectNone();
                    fsm.addToSelection(fsm.getVisualEvents(symbol));
                }, "<html>Select all events for symbol <i>" + symbolName + "</i></html>");

        return new PropertyDeclaration<>(TextAction.class, "Symbol " + symbolName,
                value -> {
                    String newName = value.getText();
                    Fsm mathFsm = fsm.getMathModel();
                    Node node = mathFsm.getNodeByReference(newName);
                    Collection<Event> events = mathFsm.getEvents(symbol);
                    if (node == null) {
                        fsm.setMathName(symbol, newName);
                    } else if (node instanceof Symbol existingSymbol) {
                        for (Event event : events) {
                            event.setSymbol(existingSymbol);
                        }
                    } else {
                        throw new FormatException("Node '" + newName + "' already exists and it is not a symbol.");
                    }
                    for (Event event : events) {
                        event.sendNotification(new PropertyChangedEvent(event, Event.PROPERTY_SYMBOL));
                    }
                },
                () -> new TextAction(symbolName).setRightAction(rightAction, false)
        ).setSpan();
    }

    public static PropertyDescriptor getEventSymbolProperty(Fsm fsm, Event event) {
        return new PropertyDeclaration<>(String.class, Event.PROPERTY_SYMBOL,
                value -> {
                    Symbol symbol = null;
                    if (!value.isEmpty()) {
                        Identifier.validate(value);
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
                    return symbol == null ? "" : fsm.getName(symbol);
                })
                .setCombinable().setTemplatable();
    }

}
