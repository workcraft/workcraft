package org.workcraft.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathNode;

class SquareNode extends VisualComponent {
    Rectangle2D.Double rectOuter;
    Rectangle2D.Double rectInner;
    int resultToReturn;

    SquareNode(Container parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
        super(null);
        this.rectOuter = rectOuter;
        this.rectInner = rectInner;
    }

    SquareNode(Container parent, Rectangle2D.Double rect) {
        this(parent, rect, rect);
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
        return Arrays.asList(new MathNode[]{});
    }

    @Override
    public void draw(DrawRequest r) {
    }

}

