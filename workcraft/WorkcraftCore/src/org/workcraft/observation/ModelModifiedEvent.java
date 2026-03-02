package org.workcraft.observation;

import org.workcraft.dom.Model;

public class ModelModifiedEvent implements StateEvent {
    private final Model sender;

    public ModelModifiedEvent(Model sender) {
        this.sender = sender;
    }

    @Override
    public Object getSender() {
        return sender;
    }
}
