package org.workcraft.testing.dom.visual;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;

class SquareNode extends VisualComponent
{
	Rectangle2D.Double rectOuter;
	Rectangle2D.Double rectInner;
	int resultToReturn;
	public SquareNode(VisualGroup parent, Rectangle2D.Double rectOuter, Rectangle2D.Double rectInner) {
		super(null);
		this.rectOuter = rectOuter;
		this.rectInner = rectInner;
	}

	public SquareNode(VisualGroup parent, Rectangle2D.Double rect) {
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
}

