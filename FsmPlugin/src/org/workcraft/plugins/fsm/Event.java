package org.workcraft.plugins.fsm;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.util.Identifier;

public class Event  extends MathConnection {
	private String symbol = "";

	public Event() {
	}

	public Event(State first, State second) {
		super(first, second);
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		if (symbol.isEmpty() || Identifier.isValid(symbol)) {
			this.symbol = symbol;
			sendNotification(new PropertyChangedEvent(this, "symbol"));
		} else {
			throw new ArgumentException("\"" + symbol + "\" is not empty and not a valid C-style identifier.\n"
					+ "The first character must be alphabetic or an underscore and the following characters must be alphanumeric or an underscore.");
		}
	}

}
