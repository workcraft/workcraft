package org.workcraft.plugins.sdfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.sdfs.VisualCounterflowLogic.class)
public class CounterflowLogic extends MathNode {
	private boolean forwardComputed = false;
	private boolean backwardComputed = false;
	private boolean forwardIndicating = true;
	private boolean backwardIndicating = true;

	public boolean isForwardComputed() {
		return forwardComputed;
	}

	public void setForwardComputed(boolean forwardComputed) {
		this.forwardComputed = forwardComputed;
		sendNotification(new PropertyChangedEvent(this, "forward computed"));
	}

	public boolean isBackwardComputed() {
		return backwardComputed;
	}

	public void setBackwardComputed(boolean backwardComputed) {
		this.backwardComputed = backwardComputed;
		sendNotification(new PropertyChangedEvent(this, "backward computed"));
	}

	public boolean isForwardIndicating() {
		return forwardIndicating;
	}

	public void setForwardIndicating(boolean forwardIndicating) {
		this.forwardIndicating = forwardIndicating;
		sendNotification(new PropertyChangedEvent(this, "forward indicating"));
	}

	public boolean isBackwardIndicating() {
		return backwardIndicating;
	}

	public void setBackwardIndicating(boolean backwardIndicating) {
		this.backwardIndicating = backwardIndicating;
		sendNotification(new PropertyChangedEvent(this, "backward indicating"));
	}
}
