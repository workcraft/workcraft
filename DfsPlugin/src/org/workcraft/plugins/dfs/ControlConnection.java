package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualControlConnection.class)
public class ControlConnection extends MathConnection {
	private boolean inverting = false;

	public ControlConnection() {
	}

	public ControlConnection (MathNode first, MathNode second) {
		super(first, second);
	}

	public boolean isInverting() {
		return inverting;
	}

	public void setInverting(boolean value) {
		this.inverting = value;
		sendNotification(new PropertyChangedEvent(this, "inverting"));
	}

}
