package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.utils.ColorUtils;
import org.workcraft.gui.tools.Decoration;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@DisplayName("Joint")
@Hotkey(KeyEvent.VK_J)
@SVGIcon("images/circuit-node-joint.svg")
public class VisualJoint extends VisualComponent {

    private static final double size = 0.25;
    public static final Shape shape = new Ellipse2D.Double(-size / 2, -size / 2, size, size);

    public VisualJoint(Joint joint) {
        super(joint);
        removePropertyDeclarationByName(PROPERTY_FILL_COLOR);
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        g.setColor(ColorUtils.colorise(getForegroundColor(), d.getColorisation()));
        g.fill(shape);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return 4 * pointInLocalSpace.distanceSq(0, 0) < size * size;
    }

}
