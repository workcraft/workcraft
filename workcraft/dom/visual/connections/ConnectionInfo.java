package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;

import org.workcraft.dom.HierarchyNode;

public interface ConnectionInfo {
	public Point2D getPoint1();
	public Point2D getPoint2();
	public HierarchyNode getConnection();
	public void update();
}
