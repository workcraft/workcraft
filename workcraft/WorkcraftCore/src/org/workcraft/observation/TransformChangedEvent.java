package org.workcraft.observation;

import org.workcraft.dom.visual.Movable;

public class TransformChangedEvent extends TransformEvent implements StateEvent {
    public TransformChangedEvent(Movable sender) {
        super(sender);
    }
}