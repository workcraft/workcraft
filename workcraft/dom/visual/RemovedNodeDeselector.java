package org.workcraft.dom.visual;

import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesDeletedEvent;

public class RemovedNodeDeselector extends HierarchySupervisor {
	private VisualModel visualModel;

	public RemovedNodeDeselector(VisualModel visualModel) {
		this.visualModel = visualModel;
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesDeletedEvent)
			visualModel.removeFromSelection(e.getAffectedNodes());
	}
}