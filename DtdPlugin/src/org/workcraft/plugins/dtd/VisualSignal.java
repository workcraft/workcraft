package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonSignalSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_X)
@DisplayName("Signal")
@SVGIcon("images/dtd-node-signal.svg")
public class VisualSignal extends VisualComponent {

    public static final String PROPERTY_COLOR = "Color";

    public VisualSignal(Signal signal) {
        super(signal);
        configureProperties();
    }

    private void configureProperties() {
        renamePropertyDeclarationByName(PROPERTY_FOREGROUND_COLOR, PROPERTY_COLOR);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);

        addPropertyDeclaration(new PropertyDeclaration<VisualSignal, Signal.Type>(
                this, Signal.PROPERTY_TYPE, Signal.Type.class, true, true, true) {
            protected void setter(VisualSignal object, Signal.Type value) {
                object.setType(value);
            }
            protected Signal.Type getter(VisualSignal object) {
                return object.getType();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualSignal, Signal.State>(
                this, Signal.PROPERTY_INITIAL_STATE, Signal.State.class, true, true, true) {
            protected void setter(VisualSignal object, Signal.State value) {
                object.setInitialState(value);
            }
            protected Signal.State getter(VisualSignal object) {
                return object.getInitialState();
            }
        });
    }

    public Signal getReferencedSignal() {
        return (Signal) getReferencedComponent();
    }

    @NoAutoSerialisation
    public Signal.Type getType() {
        return getReferencedSignal().getType();
    }

    @NoAutoSerialisation
    public void setType(Signal.Type value) {
        getReferencedSignal().setType(value);
    }

    @NoAutoSerialisation
    public Signal.State getInitialState() {
        return getReferencedSignal().getInitialState();
    }

    @NoAutoSerialisation
    public void setInitialState(Signal.State value) {
        getReferencedSignal().setInitialState(value);
    }

    public Shape getShape() {
        double h = 0.2 * size;
        double w = 0.1 * size;
        double w2 = 0.5 * w;
        Path2D shape = new Path2D.Double();
        // "1" symbol
        Path2D oneShape = new Path2D.Double();
        oneShape.moveTo(-w2, -0.5 * size + w2);
        oneShape.lineTo(0.0, -0.5 * size);
        oneShape.lineTo(0.0, -0.5 * size + h);
        oneShape.moveTo(-w2, -0.5 * size + h);
        oneShape.lineTo(+w2, -0.5 * size + h);
        shape.append(oneShape, false);
        // "0" shape
        Ellipse2D zeroShape = new Ellipse2D.Double(-w2, 0.5 * size - h, w, h);
        shape.append(zeroShape, false);
        return shape;
    }

    public Shape getInitialStateShape() {
        double h = 0.2 * size;
        double w = 0.1 * size;
        double w2 = 0.5 * w;
        double h2 = 0.5 * h;
        Path2D shape = new Path2D.Double();
        if (getReferencedSignal() != null) {
            switch (getInitialState()) {
            case HIGH:
                Path2D highShape = new Path2D.Double();
                highShape.moveTo(0.0, 0.0);
                highShape.lineTo(0.0, -h2);
                highShape.moveTo(0.0, -h);
                highShape.lineTo(-w2, -h2);
                highShape.lineTo(+w2, -h2);
                highShape.closePath();
                shape.append(highShape, false);
                break;
            case LOW:
                Path2D lowShape = new Path2D.Double();
                lowShape.moveTo(0.0, 0.0);
                lowShape.lineTo(0.0, +h2);
                lowShape.moveTo(0.0, +h);
                lowShape.lineTo(-w2, +h2);
                lowShape.lineTo(+w2, +h2);
                lowShape.closePath();
                shape.append(lowShape, false);
                break;
            case UNSTABLE:
                Path2D unstableShape = new Path2D.Double();
                unstableShape.moveTo(-w2, 0.0);
                unstableShape.lineTo(0.0, -h2);
                unstableShape.lineTo(0.0, +h2);
                unstableShape.lineTo(+w2, 0.0);
                shape.append(unstableShape, false);
                break;
            case STABLE:
                Line2D stableShape = new Line2D.Double(-w2, 0.0, +w2, 0.0);
                shape.append(stableShape, false);
                break;
            }
        }
        return shape;
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Color colorisation = r.getDecoration().getColorisation();
        g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
        g.setStroke(new BasicStroke(0.2f * (float) strokeWidth));
        g.draw(getShape());
        g.setStroke(new BasicStroke(0.3f * (float) strokeWidth));
        g.draw(getInitialStateShape());
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return BoundingBoxHelper.expand(getShape().getBounds2D(), 0.5 * size, 0.5 * size);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }

    @Override
    public Positioning getNamePositioning() {
        return Positioning.LEFT;
    }

    @Override
    public boolean getLabelVisibility() {
        return false;
    }

    @Override
    public boolean getNameVisibility() {
        return true;
    }

    @Override
    public Color getNameColor() {
        switch (getType()) {
        case INPUT:    return CommonSignalSettings.getInputColor();
        case OUTPUT:   return CommonSignalSettings.getOutputColor();
        case INTERNAL: return CommonSignalSettings.getInternalColor();
        default:       return CommonSignalSettings.getDummyColor();
        }
    }
}
