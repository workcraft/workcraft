package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualCounterflowRegister.class)
public class CounterflowRegister extends MathDelayNode {
	private boolean orMarked = false;
	private boolean andMarked = false;

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
