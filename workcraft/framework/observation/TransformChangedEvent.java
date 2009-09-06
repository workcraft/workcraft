package org.workcraft.framework.observation;

import org.workcraft.dom.visual.VisualNode;


public class TransformChangedEvent implements StateEvent {
	private VisualNode sender;

	public TransformChangedEvent(VisualNode sender) {
		this.sender = sender;
	}

	public VisualNode getSender() {
		return sender;
	}
}
