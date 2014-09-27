package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("State")
@VisualClass(org.workcraft.plugins.fsm.VisualState.class)
public class State extends MathNode {
	private boolean initial = false;
	private boolean terminal = false;

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean value) {
		if (initial != value) {
			initial = value;
			sendNotification(new PropertyChangedEvent(this, "initial"));
		}
	}

	public boolean isTerminal() {
		return terminal;
	}

	public void setTerminal(boolean value) {
		if (terminal != value) {
			terminal = value;
			sendNotification(new PropertyChangedEvent(this, "terminal"));
		}
	}

}
