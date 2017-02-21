package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.graph.VisualVertex;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Transition")
@SVGIcon("images/dtd-node-transition.svg")
public class VisualTransition extends VisualVertex {

    public static final String PROPERTY_COLOR = "Color";

    public VisualTransition(Transition transition) {
        super(transition);
        renamePropertyDeclarationByName(PROPERTY_FOREGROUND_COLOR, PROPERTY_COLOR);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);
        removePropertyDeclarationByName(PROPERTY_SYMBOL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_SYMBOL_COLOR);
        removePropertyDeclarationByName(PROPERTY_RENDER_TYPE);
    }

    public Shape getShape() {
        Path2D shape = new Path2D.Double();
        if (getReferencedTransition() != null) {
            double sw2 = 0.5 * strokeWidth;
            double w = 0.08 * size;
            double w2 = 0.5 * w;
            double h = 0.1 * size;
            double s = 0.5 * size;
            double s2 = 0.5 * s;
            switch (getReferencedTransition().getDirection()) {
            case RISE:
                shape.moveTo(0.0, +s2);
                shape.lineTo(0.0, -s2 + h);
                shape.moveTo(0.0, -s2 + sw2);
                shape.lineTo(+w2, -s2 + h);
                shape.lineTo(-w2, -s2 + h);
                shape.closePath();
                break;
            case FALL:
                shape.moveTo(0.0, -s2);
                shape.lineTo(0.0, +s2 - h);
                shape.moveTo(0.0, +s2 - sw2);
                shape.lineTo(-w2, +s2 - h);
                shape.lineTo(+w2, +s2 - h);
                shape.closePath();
                break;
            case DESTABILISE:
                shape.moveTo(0.0, +s2 - sw2);
                shape.lineTo(0.0, +h);
                shape.moveTo(0.0, +sw2);
                shape.lineTo(+w2, +h);
                shape.lineTo(-w2, +h);
                shape.closePath();
                shape.moveTo(0.0, -s2 + sw2);
                shape.lineTo(0.0, -h);
                shape.moveTo(0.0, -sw2);
                shape.lineTo(-w2, -h);
                shape.lineTo(+w2, -h);
                shape.closePath();
                break;
            case STABILISE:
                shape.moveTo(0.0, +0.0);
                shape.lineTo(0.0, -s2 + h);
                shape.moveTo(0.0, -s2 + sw2);
                shape.lineTo(+w2, -s2 + h);
                shape.lineTo(-w2, -s2 + h);
                shape.closePath();
                shape.moveTo(0.0, -0.0);
                shape.lineTo(0.0, +s2 - h);
                shape.moveTo(0.0, +s2 - sw2);
                shape.lineTo(-w2, +s2 - h);
                shape.lineTo(+w2, +s2 - h);
                shape.closePath();
                break;
            }
        }
        return shape;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        Shape shape = getShape();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(new BasicStroke((float) strokeWidth / 2.0f));
        g.draw(shape);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

    public Transition getReferencedTransition() {
        return (Transition) getReferencedComponent();
    }

    public Signal getSignal() {
        return getReferencedTransition().getSignal();
    }

    public Direction getDirection() {
        return getReferencedTransition().getDirection();
    }

    @Override
    public boolean getLabelVisibility() {
        return false;
    }

    @Override
    public boolean getNameVisibility() {
        return false;
    }

}
