package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Event")
@VisualClass(org.workcraft.plugins.fsm.VisualEvent.class)
public class Event extends MathConnection {

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

	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
		sendNotification(new PropertyChangedEvent(this, "symbol"));
	}

}
