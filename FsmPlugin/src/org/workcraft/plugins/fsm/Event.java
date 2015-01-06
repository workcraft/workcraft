package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Event")
@VisualClass(org.workcraft.plugins.fsm.VisualEvent.class)
public class Event extends MathConnection {

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
