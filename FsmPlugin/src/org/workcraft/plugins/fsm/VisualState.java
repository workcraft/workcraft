package org.workcraft.plugins.fsm;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;

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
        addPropertyDeclaration(new PropertyDeclaration<VisualState, Boolean>(
                this, State.PROPERTY_INITIAL, Boolean.class, true, false, false) {
            public void setter(VisualState object, Boolean value) {
                object.getReferencedState().setInitial(value);
            }
            public Boolean getter(VisualState object) {
                return object.getReferencedState().isInitial();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualState, Positioning>(
                this, PROPERTY_INITIAL_MARKER_POSITIONING, Positioning.class, true, true, true) {
            protected void setter(VisualState object, Positioning value) {
                object.setInitialMarkerPositioning(value);
            }
            protected Positioning getter(VisualState object) {
                return object.getInitialMarkerPositioning();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualState, Boolean>(
                this, State.PROPERTY_FINAL, Boolean.class, true, true, true) {
            public void setter(VisualState object, Boolean value) {
                object.getReferencedState().setFinal(value);
            }
            public Boolean getter(VisualState object) {
                return object.getReferencedState().isFinal();
            }
        });
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
        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.draw(shape);

        if (getReferencedState().isInitial()) {
            g.setStroke(new BasicStroke(strokeWidth));
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
            if (getInitialMarkerPositioning() == Positioning.CENTER) {
                g.fill(getInitialMarkerCenterShape());
            } else {
                AffineTransform savedTransform = g.getTransform();
                AffineTransform rotateTransform = getInitialMarkerPositioning().getTransform();
                g.transform(rotateTransform);
                g.draw(getInitialMarkerShape());
                g.setTransform(savedTransform);
            }
        }

        if (getReferencedState().isFinal()) {
            g.setStroke(new BasicStroke(strokeWidth / 2));
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
            g.draw(getFinalMarkerShape());
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    private Shape getInitialMarkerShape() {
        double arrowSize = size / 4;
        Path2D shape = new Path2D.Double();
        shape.moveTo(0.0, -size - strokeWidth);
        shape.lineTo(0.0, -size / 2 - strokeWidth);
        shape.moveTo(-arrowSize / 2, -size / 2 - strokeWidth / 2 - arrowSize);
        shape.lineTo(0.0, -size / 2 - strokeWidth / 2);
        shape.lineTo(arrowSize / 2, -size / 2 - strokeWidth / 2 - arrowSize);
        return shape;
    }

    private Shape getInitialMarkerCenterShape() {
        double s = size / 4;
        return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
    }

    private Shape getFinalMarkerShape() {
        double s = 2 * size / 3;
        return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getReferencedState().isInitial()) {
            bb = BoundingBoxHelper.union(bb, getInitialMarkerShape().getBounds2D());
        }
        if (getReferencedState().isFinal()) {
            bb = BoundingBoxHelper.union(bb, getFinalMarkerShape().getBounds2D());
        }
        return bb;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    public State getReferencedState() {
        return (State) getReferencedComponent();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualState) {
            VisualState srcComponent = (VisualState) src;
            getReferencedState().setFinal(srcComponent.getReferencedState().isFinal());
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
                if (srcState.getReferencedState().isFinal()) {
                    isFinal = true;
                }
                if (srcState.getReferencedState().isInitial()) {
                    isInitial = true;
                }
                initialMarkerPositioning.add(srcState.getInitialMarkerPositioning());
            }
        }
        getReferencedState().setFinal(isFinal);
        getReferencedState().setInitial(isInitial);
        setInitialMarkerPositioning(MixUtils.vote(initialMarkerPositioning, Positioning.class, Positioning.TOP));
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
