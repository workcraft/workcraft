package org.workcraft.observation;

import org.workcraft.dom.visual.Movable;

public class TransformChangingEvent extends TransformEvent implements StateEvent {
    public TransformChangingEvent(Movable sender) {
        super(sender);
    }
}
