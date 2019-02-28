package org.workcraft.plugins.dtd;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.geom.Point2D;

public abstract class VisualEvent extends VisualComponent implements CustomTouchable {

    public VisualEvent(Event event) {
        super(event);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);
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

    public abstract BasicStroke getStroke();

}
