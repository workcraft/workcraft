package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;

public class VisualSignalEntry extends VisualSignalEvent {

    public VisualSignalEntry(SignalEntry entry) {
        super(entry);
    }

    @Override
    public Shape getShape() {
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
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Shape shape = getShape();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(new BasicStroke((float) strokeWidth / 5.0f));
        g.draw(shape);
    }

}
