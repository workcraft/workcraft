package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualCounterflowLogic.class)
public class CounterflowLogic extends MathDelayNode {
	public static final String PROPERTY_FORWARD_COMPUTED = "Forward computed";
	public static final String PROPERTY_BACKWARD_COMPUTED = "Backward computed";
	public static final String PROPERTY_FORWARD_EARLY_EVALUATION = "Forward early evaluation";
	public static final String PROPERTY_BACKWARD_EARLY_EVALUATION = "Backward early evaluation";
	private boolean forwardComputed = false;
	private boolean backwardComputed = false;
	private boolean forwardEarlyEvaluation = false;
	private boolean backwardEarlyEvaluation = false;

	public boolean isForwardComputed() {
		return forwardComputed;
	}

	public void setForwardComputed(boolean value) {
		this.forwardComputed = value;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_FORWARD_COMPUTED));
	}

	public boolean isBackwardComputed() {
		return backwardComputed;
	}

	public void setBackwardComputed(boolean value) {
		this.backwardComputed = value;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_BACKWARD_COMPUTED));
	}

	public boolean isForwardEarlyEvaluation() {
		return forwardEarlyEvaluation;
	}

	public void setForwardEarlyEvaluation(boolean value) {
		this.forwardEarlyEvaluation = value;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_FORWARD_EARLY_EVALUATION));
	}

	public boolean isBackwardEarlyEvaluation() {
		return backwardEarlyEvaluation;
	}

	public void setBackwardEarlyEvaluation(boolean value) {
		this.backwardEarlyEvaluation = value;
		sendNotification(new PropertyChangedEvent(this, PROPERTY_BACKWARD_EARLY_EVALUATION));
	}
}
