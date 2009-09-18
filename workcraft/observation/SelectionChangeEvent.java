package org.workcraft.observation;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;

public class SelectionChangeEvent implements StateEvent {
	private VisualModel sender;

	public SelectionChangeEvent(VisualModel sender) {
		this.sender = sender;
	}

	public VisualModel getSender() {
		return sender;
	}

	public Collection<Node> getSelection() {
		return sender.getSelection();
	}

}
