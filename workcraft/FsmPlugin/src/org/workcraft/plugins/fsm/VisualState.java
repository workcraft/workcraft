package org.workcraft.plugins.fsm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.*;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;
import java.util.LinkedList;

@Hotkey(KeyEvent.VK_T)
@DisplayName("State")
@SVGIcon("images/fsm-node-vertex.svg")
public class VisualState extends VisualComponent {

    public static final String PROPERTY_INITIAL_MARKER_POSITIONING = "Initial marker positioning";

    private static double size = 1.0;
    private static float strokeWidth = 0.1f;
    private Positioning initialMarkerPositioning = Positioning.TOP;

    public VisualState(State state) {
        super(state);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, State.PROPERTY_INITIAL,
                value -> getReferencedComponent().setInitial(value),
                () -> getReferencedComponent().isInitial()));

        addPropertyDeclaration(new PropertyDeclaration<>(Positioning.class, PROPERTY_INITIAL_MARKER_POSITIONING,
                this::setInitialMarkerPositioning, this::getInitialMarkerPositioning).setCombinable().setTemplatable());

        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, State.PROPERTY_FINAL,
                value -> getReferencedComponent().setFinal(value),
                () -> getReferencedComponent().isFinal())
                .setCombinable().setTemplatable());
    }

    public Positioning getInitialMarkerPositioning() {
        return initialMarkerPositioning;
    }

    public void setInitialMarkerPositioning(Positioning value) {
        if (initialMarkerPositioning != value) {
            initialMarkerPositioning = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INITIAL_MARKER_POSITIONING));
        }
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        double s = size - strokeWidth;
        Shape shape = new Ellipse2D.Double(-s / 2, -s / 2, s, s);
        g.setColor(ColorUtils.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.draw(shape);

        if (getReferencedComponent().isInitial()) {
            g.setStroke(new BasicStroke(strokeWidth));
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
            AffineTransform savedTransform = g.getTransform();
            AffineTransform rotateTransform = getInitialMarkerPositioning().getTransform();
            g.transform(rotateTransform);
            if (getInitialMarkerPositioning() == Positioning.CENTER) {
                g.fill(getInitialMarkerShape());
            } else {
                g.draw(getInitialMarkerShape());
            }
            g.setTransform(savedTransform);
        }

        if (getReferencedComponent().isFinal()) {
            g.setStroke(new BasicStroke(strokeWidth / 2));
            g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
            g.draw(getFinalMarkerShape());
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    private Shape getInitialMarkerShape() {
        double s = size / 4;
        if (getInitialMarkerPositioning() == Positioning.CENTER) {
            return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
        }
        Path2D shape = new Path2D.Double();
        shape.moveTo(0.0, -size - strokeWidth);
        shape.lineTo(0.0, -size / 2 - strokeWidth);
        shape.moveTo(-s / 2, -size / 2 - strokeWidth / 2 - s);
        shape.lineTo(0.0, -size / 2 - strokeWidth / 2);
        shape.lineTo(s / 2, -size / 2 - strokeWidth / 2 - s);
        return shape;
    }

    private Shape getFinalMarkerShape() {
        double s = 2 * size / 3;
        return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D result = super.getBoundingBoxInLocalSpace();
        if (getReferencedComponent().isInitial()) {
            AffineTransform rotateTransform = getInitialMarkerPositioning().getTransform();
            Shape shape = getInitialMarkerShape();
            Rectangle2D bb = BoundingBoxHelper.transform(shape.getBounds2D(), rotateTransform);
            result = BoundingBoxHelper.union(result, bb);
        }
        if (getReferencedComponent().isFinal()) {
            result = BoundingBoxHelper.union(result, getFinalMarkerShape().getBounds2D());
        }
        return result;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    @Override
    public State getReferencedComponent() {
        return (State) super.getReferencedComponent();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualState) {
            VisualState srcComponent = (VisualState) src;
            getReferencedComponent().setFinal(srcComponent.getReferencedComponent().isFinal());
            setInitialMarkerPositioning(((VisualState) src).getInitialMarkerPositioning());
        }
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        boolean isFinal = false;
        boolean isInitial = false;
        LinkedList<Positioning> initialMarkerPositioning = new LinkedList<>();
        for (Stylable src: srcs) {
            if (src instanceof VisualState) {
                VisualState srcState = (VisualState) src;
                if (srcState.getReferencedComponent().isFinal()) {
                    isFinal = true;
                }
                if (srcState.getReferencedComponent().isInitial()) {
                    isInitial = true;
                }
                initialMarkerPositioning.add(srcState.getInitialMarkerPositioning());
            }
        }
        getReferencedComponent().setFinal(isFinal);
        getReferencedComponent().setInitial(isInitial);
        setInitialMarkerPositioning(MixUtils.vote(initialMarkerPositioning, Positioning.TOP));
    }

    @Override
    public void rotateClockwise() {
        setInitialMarkerPositioning(getInitialMarkerPositioning().rotateClockwise());
        super.rotateClockwise();
    }

    @Override
    public void rotateCounterclockwise() {
        setInitialMarkerPositioning(getInitialMarkerPositioning().rotateCounterclockwise());
        super.rotateCounterclockwise();
    }

    @Override
    public void flipHorizontal() {
        setInitialMarkerPositioning(getInitialMarkerPositioning().flipHorizontal());
        super.flipHorizontal();
    }

    @Override
    public void flipVertical() {
        setInitialMarkerPositioning(getInitialMarkerPositioning().flipVertical());
        super.flipVertical();
    }

}
