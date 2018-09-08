package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;

public abstract class VisualEvent extends VisualComponent implements CustomTouchable {

    public VisualEvent(Event event) {
        super(event);
    }

    public Event getReferencedSignalEvent() {
        return (Event) getReferencedComponent();
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(getStroke());
        g.draw(getShape());
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    @Override
    public boolean getLabelVisibility() {
        return false;
    }

    @Override
    public boolean getNameVisibility() {
        return false;
    }

    @Override
    public Node hitCustom(Point2D point) {
        Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
        return hitTestInLocalSpace(pointInLocalSpace) ? this : null;
    }

    public VisualSignal getVisualSignal() {
        return (VisualSignal) getParent();
    }

    public abstract Shape getShape();

    public abstract BasicStroke getStroke();

}
