package org.workcraft.observation;

import org.workcraft.dom.Node;

public class PropertyChangedEvent implements StateEvent {

    private final Node sender;
    private final String propertyName;

    public PropertyChangedEvent(Node sender, String propertyName) {
        this.sender = sender;
        this.propertyName = propertyName;
    }

    @Override
    public Node getSender() {
        return sender;
    }

    public String getPropertyName() {
        return propertyName;
    }

}
