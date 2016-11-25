package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;

@DisplayName("ChannelPlace")
//@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/son-node-channel_place.svg")
public class VisualChannelPlace extends VisualPlaceNode {

    private static float strokeWidth = 0.2f;
    protected double size = 1.2;

    public VisualChannelPlace(ChannelPlace cplace) {
        super(cplace);
        removePropertyDeclarationByName("Initial");
        removePropertyDeclarationByName("Final");
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

        drawToken(r);
        drawErrorInLocalSpace(r);
        drawDurationInLocalSpace(r);
        drawLabelInLocalSpace(r);
        drawNameInLocalSpace(r);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }
}
