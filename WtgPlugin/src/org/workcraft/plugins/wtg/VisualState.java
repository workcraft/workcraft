package org.workcraft.plugins.wtg;

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
import org.workcraft.plugins.wtg.decorations.StateDecoration;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

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
    public Shape getShape() {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
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
        } else if (getReferencedState().isInitial()) {
            g.fill(getInitialMarkerShape());
        }
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

    public State getReferencedState() {
        return (State) getReferencedComponent();
    }

}
