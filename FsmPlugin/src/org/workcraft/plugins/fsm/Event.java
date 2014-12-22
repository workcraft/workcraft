package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.util.Identifier;

@DisplayName("Event")
@VisualClass(org.workcraft.plugins.fsm.VisualEvent.class)
public class Event extends MathConnection {

	public enum Type {
		INPUT,
		OUTPUT,
		INTERNAL;

		@Override
		public String toString() {
			switch(this)
			{
			case INPUT:
				return "input";
			case OUTPUT:
				return "output";
			case INTERNAL:
				return "internal";
			default:
				throw new NotSupportedException();
			}
		}
	}

	public enum Direction {
		PLUS,
		MINUS,
		TOGGLE;

		public static Direction fromString(String s) {
			if (s.equals("+"))
				return PLUS;
			else if (s.equals("-"))
				return MINUS;
			else if (s.equals("~"))
				return TOGGLE;

			throw new ArgumentException ("Unexpected string: " + s);
		}

		@Override
		public String toString() {
			switch(this)
			{
			case PLUS:
				return "+";
			case MINUS:
				return "-";
			case TOGGLE:
				return "~";
			default:
				throw new NotSupportedException();
			}
		}
	}

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
