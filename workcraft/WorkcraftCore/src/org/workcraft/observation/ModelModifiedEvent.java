package org.workcraft.observation;

import org.workcraft.dom.visual.VisualModel;

public class ModelModifiedEvent implements StateEvent {
    private final VisualModel sender;

    public ModelModifiedEvent(VisualModel sender) {
        this.sender = sender;
    }

    @Override
    public Object getSender() {
        return sender;
    }
}
