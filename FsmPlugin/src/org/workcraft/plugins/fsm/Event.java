package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Event")
@IdentifierPrefix(value = "e", isInternal = true)
@VisualClass(org.workcraft.plugins.fsm.VisualEvent.class)
public class Event extends MathConnection {
    public static final String PROPERTY_SYMBOL = "Symbol";

    private Symbol symbol;

    public Event() {
    }

    public Event(State first, State second, Symbol symbol) {
        super(first, second);
        this.setSymbol(symbol);
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol value) {
        if (symbol != value) {
            symbol = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SYMBOL));
        }
    }

}
