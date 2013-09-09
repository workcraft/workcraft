package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualCounterflowLogic.class)
public class CounterflowLogic extends MathNode {
	private boolean forwardComputed = false;
	private boolean backwardComputed = false;
	private boolean forwardEarlyEvaluation = false;
	private boolean backwardEarlyEvaluation = false;

	public boolean isForwardComputed() {
		return forwardComputed;
	}

	public void setForwardComputed(boolean value) {
		this.forwardComputed = value;
		sendNotification(new PropertyChangedEvent(this, "forward computed"));
	}

	public boolean isBackwardComputed() {
		return backwardComputed;
	}

	public void setBackwardComputed(boolean value) {
		this.backwardComputed = value;
		sendNotification(new PropertyChangedEvent(this, "backward computed"));
	}

	public boolean isForwardEarlyEvaluation() {
		return forwardEarlyEvaluation;
	}

	public void setForwardEarlyEvaluation(boolean value) {
		this.forwardEarlyEvaluation = value;
		sendNotification(new PropertyChangedEvent(this, "forward early evaluation"));
	}

	public boolean isBackwardEarlyEvaluation() {
		return backwardEarlyEvaluation;
	}

	public void setBackwardEarlyEvaluation(boolean value) {
		this.backwardEarlyEvaluation = value;
		sendNotification(new PropertyChangedEvent(this, "backward early evaluation"));
	}
}
