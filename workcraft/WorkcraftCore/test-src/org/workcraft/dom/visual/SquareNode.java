package org.workcraft.dom.visual;

import org.workcraft.dom.math.MathNode;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

class SquareNode extends VisualComponent {

    private final Rectangle2D.Double rectOuter;
    private final Rectangle2D.Double rectInner;

    SquareNode(Rectangle2D.Double rect) {
        this(rect, rect);
    }

    SquareNode(Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
        super(null);
        this.rectOuter = rectOuter;
        this.rectInner = rectInner;
    }

    @Override
    public String toString() {
        return rectInner.toString();
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        return rectOuter;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return rectInner.contains(pointInLocalSpace);
    }

    @Override
    public Collection<MathNode> getMathReferences() {
        return List.of();
    }

    @Override
    public void draw(DrawRequest r) {
    }

}

