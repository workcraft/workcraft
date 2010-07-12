package org.workcraft.dom.visual;

import java.awt.geom.Point2D;

import org.workcraft.dom.Node;

public interface CustomTouchable
{
	Node customHitTest(Point2D point);
}
