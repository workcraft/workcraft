package org.workcraft.plugins.wtg;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;

@Hotkey(KeyEvent.VK_Q)
@DisplayName("State")
@SVGIcon("images/wtg-node-state.svg")
public class VisualState extends VisualComponent {
    private static double tokenSize = CommonVisualSettings.getNodeSize() / 1.9;

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
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();

        double s = size - strokeWidth;
        Shape shape = new Ellipse2D.Double(-s / 2, -s / 2, s, s);
        g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
        g.fill(shape);
        g.setStroke(new BasicStroke((float) strokeWidth));
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.draw(shape);

        if (getReferencedState().isInitial()) {
            g.fill(getInitialMarkerShape());
        }

        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    private Shape getInitialMarkerShape() {
        return new Ellipse2D.Double(-tokenSize / 2, -tokenSize / 2, tokenSize, tokenSize);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getReferencedState().isInitial()) {
            bb = BoundingBoxHelper.union(bb, getInitialMarkerShape().getBounds2D());
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

}
