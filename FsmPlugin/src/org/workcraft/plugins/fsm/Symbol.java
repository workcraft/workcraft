package org.workcraft.plugins.fsm;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NotSupportedException;

public class Symbol extends MathNode {
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

	public Symbol() {
	}

}

