package org.workcraft.dom.visual;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletedEvent;

public class RemovedNodeDeselector extends HierarchySupervisor {
    private final VisualModel visualModel;

    public RemovedNodeDeselector(VisualModel visualModel) {
        this.visualModel = visualModel;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletedEvent) {
            visualModel.removeFromSelection(e.getAffectedNodes());
        }
    }
}
