package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.HierarchyNode;

public interface Movable extends HierarchyNode {
	public AffineTransform getTransform();
	public void applyTransform(AffineTransform transform);
}