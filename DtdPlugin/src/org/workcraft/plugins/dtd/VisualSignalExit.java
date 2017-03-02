package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Path2D;

public class VisualSignalExit extends VisualSignalEvent {

    public VisualSignalExit(SignalExit exit) {
        super(exit);
    }

    @Override
    public Shape getShape() {
        double w2 = 0.04 * size;
        double s2 = 0.25 * size;
        Path2D shape = new Path2D.Double();
        shape.moveTo(0.0 + w2, +s2);
        shape.lineTo(0.0 - w2, +s2);
        shape.lineTo(0.0 - w2, -s2);
        shape.lineTo(0.0 + w2, -s2);
        return shape;
    }

    @Override
    public BasicStroke getStroke() {
        return new BasicStroke((float) strokeWidth / 10.0f);
    }

}
