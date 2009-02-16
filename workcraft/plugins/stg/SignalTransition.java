package org.workcraft.plugins.stg;

import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.plugins.petri.Transition;

@DisplayName("Signal transition")
@VisualClass("org.workcraft.plugins.stg.VisualSignalTransition")
public class SignalTransition extends Transition {
	public enum Type {
		INPUT,
		OUTPUT,
		INTERNAL,
		DUMMY
	}

	private Type type = Type.INTERNAL;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
