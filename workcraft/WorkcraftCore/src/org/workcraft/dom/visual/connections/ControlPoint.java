package org.workcraft.dom.visual.connections;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ControlPoint extends VisualTransformableNode implements Drawable {

    private static final double size = 0.2;
    private static final Color fillColor = Color.BLUE;
    private static final Shape shape = new Ellipse2D.Double(-size / 2, -size / 2, size, size);

    public ControlPoint() {
        setHidden(true);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        return new Rectangle2D.Double(-size, -size, size * 2, size * 2);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        g.setColor(ColorUtils.colorise(fillColor, colorisation));
        g.fill(shape);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

}
