package org.workcraft.observation;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.visual.Movable;

public class TransformEvent {
    public Movable sender;

    public TransformEvent(Movable sender) {
        this.sender = sender;
    }

    public Movable getSender() {
        return sender;
    }

    public AffineTransform getTransform() {
        return sender.getTransform();
    }
}