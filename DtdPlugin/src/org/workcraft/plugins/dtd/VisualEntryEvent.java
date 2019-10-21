package org.workcraft.plugins.dtd;

import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Path2D;

public class VisualEntryEvent extends VisualEvent {

    public VisualEntryEvent(EntryEvent entry) {
        super(entry);
    }

    @Override
    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize();
        double w2 = 0.04 * size;
        double s2 = 0.25 * size;
        Path2D shape = new Path2D.Double();
        shape.moveTo(0.0 - w2, +s2);
        shape.lineTo(0.0 + w2, +s2);
        shape.lineTo(0.0 + w2, -s2);
        shape.lineTo(0.0 - w2, -s2);
        return shape;
    }

    @Override
    public BasicStroke getStroke() {
        return new BasicStroke((float) VisualCommonSettings.getStrokeWidth() / 10.0f);
    }

}
