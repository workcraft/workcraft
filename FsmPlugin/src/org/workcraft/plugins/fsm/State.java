package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("State")
@VisualClass(org.workcraft.plugins.fsm.VisualState.class)
public class State extends MathNode {
	private boolean initialState = false;
	private boolean finalState = false;

	public boolean isInitial() {
		return initialState;
	}

	public void setInitial(boolean value) {
		if (setInitialQuiet(value)) {
			sendNotification(new PropertyChangedEvent(this, "initial"));
		}
	}

	public boolean setInitialQuiet(boolean value) {
		if (initialState != value) {
			initialState = value;
			return true;
		}
		return false;
	}

	public boolean isFinal() {
		return finalState;
	}

	public void setFinal(boolean value) {
		if (finalState != value) {
			finalState = value;
			sendNotification(new PropertyChangedEvent(this, "final"));
		}
	}

}
