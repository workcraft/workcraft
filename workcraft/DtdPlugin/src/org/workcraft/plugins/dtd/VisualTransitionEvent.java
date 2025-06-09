package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.serialisation.NoAutoSerialisation;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Path2D.Double;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Transition")
@SVGIcon("images/dtd-node-transition.svg")
public class VisualTransitionEvent extends VisualEvent {

    public VisualTransitionEvent(TransitionEvent transition) {
        super(transition);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(TransitionEvent.Direction.class, TransitionEvent.PROPERTY_DIRECTION,
                this::setDirection, this::getDirection).setCombinable().setTemplatable());
    }

    private Double getShapeEmpty() {
        return new Path2D.Double();
    }

    private Shape getShapeRise(double w, double h, double s) {
        Path2D shape = getShapeEmpty();
        double sw2 = 0.5 * VisualCommonSettings.getStrokeWidth();
        double w2 = 0.5 * w;
        double s2 = 0.5 * s;
        shape.moveTo(0.0, +s2);
        shape.lineTo(0.0, -s2 + h);
        shape.moveTo(0.0, -s2 + sw2);
        shape.lineTo(+w2, -s2 + h);
        shape.lineTo(-w2, -s2 + h);
        shape.closePath();
        return shape;
    }

    private Shape getShapeFall(double w, double h, double s) {
        Path2D shape = getShapeEmpty();
        double sw2 = 0.5 * VisualCommonSettings.getStrokeWidth();
        double w2 = 0.5 * w;
        double s2 = 0.5 * s;
        shape.moveTo(0.0, -s2);
        shape.lineTo(0.0, s2 - h);
        shape.moveTo(0.0, s2 - sw2);
        shape.lineTo(-w2, s2 - h);
        shape.lineTo(+w2, s2 - h);
        shape.closePath();
        return shape;
    }

    private Shape getShapeDestabilise(double w, double h, double s) {
        Path2D shape = getShapeEmpty();
        double sw2 = 0.5 * VisualCommonSettings.getStrokeWidth();
        double w2 = 0.5 * w;
        double s2 = 0.5 * s;
        shape.moveTo(0.0, s2 - sw2);
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
        return shape;
    }

    private Shape getShapeStabilise(double w, double h, double s) {
        Path2D shape = getShapeEmpty();
        double sw2 = 0.5 * VisualCommonSettings.getStrokeWidth();
        double w2 = 0.5 * w;
        double s2 = 0.5 * s;
        shape.moveTo(0.0, +0.0);
        shape.lineTo(0.0, -s2 + h);
        shape.moveTo(0.0, -s2 + sw2);
        shape.lineTo(+w2, -s2 + h);
        shape.lineTo(-w2, -s2 + h);
        shape.closePath();
        shape.moveTo(0.0, -0.0);
        shape.lineTo(0.0, s2 - h);
        shape.moveTo(0.0, s2 - sw2);
        shape.lineTo(-w2, s2 - h);
        shape.lineTo(+w2, s2 - h);
        shape.closePath();
        return shape;
    }

    @Override
    public Shape getShape() {
        if (getReferencedComponent() == null) {
            return getShapeEmpty();
        }
        double size = VisualCommonSettings.getNodeSize();
        double w = 0.08 * size;
        double h = 0.1 * size;
        double s = 0.5 * size;
        return switch (getReferencedComponent().getDirection()) {
            case RISE -> getShapeRise(w, h, s);
            case FALL -> getShapeFall(w, h, s);
            case DESTABILISE -> getShapeDestabilise(w, h, s);
            case STABILISE -> getShapeStabilise(w, h, s);
        };
    }

    @Override
    public BasicStroke getStroke() {
        return new BasicStroke((float) VisualCommonSettings.getStrokeWidth() / 2.0f);
    }

    @Override
    public TransitionEvent getReferencedComponent() {
        return (TransitionEvent) super.getReferencedComponent();
    }

    @NoAutoSerialisation
    public void setDirection(TransitionEvent.Direction value) {
        getReferencedComponent().setDirection(value);
    }

    @NoAutoSerialisation
    public TransitionEvent.Direction getDirection() {
        return getReferencedComponent().getDirection();
    }

}
