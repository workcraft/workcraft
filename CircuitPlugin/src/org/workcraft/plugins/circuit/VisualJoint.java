package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;

@DisplayName("Joint")
@Hotkey(KeyEvent.VK_J)
@SVGIcon("images/circuit-node-joint.svg")
public class VisualJoint extends VisualComponent {
    public static double size = 0.25;
    public static final Shape shape = new Ellipse2D.Double(-size / 2, -size / 2, size, size);

    public VisualJoint(Joint joint) {
        super(joint);
        removePropertyDeclarationByName("Fill color");
        removePropertyDeclarationByName("Label");
        removePropertyDeclarationByName("Label color");
        removePropertyDeclarationByName("Label positioning");
        removePropertyDeclarationByName("Name color");
        removePropertyDeclarationByName("Name positioning");
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.fill(shape);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        return new Rectangle2D.Double(-size / 2, -size / 2, size, size);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

}
