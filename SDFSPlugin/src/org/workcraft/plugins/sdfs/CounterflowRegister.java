package org.workcraft.plugins.sdfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.sdfs.VisualCounterflowRegister.class)
public class CounterflowRegister  extends MathNode {
	private boolean forwardEnabled = false;
	private boolean backwardEnabled = false;
	private boolean orMarked = false;
	private boolean andMarked = false;

	public boolean isForwardEnabled() {
		return forwardEnabled;
	}

	public void setForwardEnabled(boolean forwardEnabled) {
		this.forwardEnabled = forwardEnabled;
		sendNotification(new PropertyChangedEvent(this, "forward enabled"));
	}

	public boolean isBackwardEnabled() {
		return backwardEnabled;
	}

	public void setBackwardEnabled(boolean backwardEnabled) {
		this.backwardEnabled = backwardEnabled;
		sendNotification(new PropertyChangedEvent(this, "backward enabled"));
	}

	public boolean isOrMarked() {
		return orMarked;
	}

	public void setOrMarked(boolean orMarked) {
		this.orMarked = orMarked;
		sendNotification(new PropertyChangedEvent(this, "or-marked"));
	}

	public boolean isAndMarked() {
		return andMarked;
	}

	public void setAndMarked(boolean andMarked) {
		this.andMarked = andMarked;
		sendNotification(new PropertyChangedEvent(this, "and-marked"));
	}
}
