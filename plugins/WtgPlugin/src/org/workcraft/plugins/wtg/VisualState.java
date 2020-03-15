package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.wtg.decorations.StateDecoration;
import org.workcraft.utils.Coloriser;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_Q)
@DisplayName("State")
@SVGIcon("images/wtg-node-state.svg")
public class VisualState extends VisualComponent {
    private static double tokenSize = VisualCommonSettings.getNodeSize() / 1.9;

    public VisualState(State state) {
        super(state);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Boolean.class, State.PROPERTY_INITIAL,
                value -> getReferencedComponent().setInitial(value),
                () -> getReferencedComponent().isInitial()));
    }

    @Override
    public Shape getShape() {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Ellipse2D.Double(pos, pos, size, size);
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        if (d instanceof StateDecoration) {
            StateDecoration sd = (StateDecoration) d;
            if (sd.isMarked()) {
                g.fill(getInitialMarkerShape());
            }
        } else if (getReferencedComponent().isInitial()) {
            g.fill(getInitialMarkerShape());
        }
    }

    private Shape getInitialMarkerShape() {
        return new Ellipse2D.Double(-tokenSize / 2, -tokenSize / 2, tokenSize, tokenSize);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getReferencedComponent().isInitial()) {
            bb = BoundingBoxHelper.union(bb, getInitialMarkerShape().getBounds2D());
        }
        return bb;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        double size = VisualCommonSettings.getNodeSize() - VisualCommonSettings.getStrokeWidth();
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    @Override
    public State getReferencedComponent() {
        return (State) super.getReferencedComponent();
    }

}
