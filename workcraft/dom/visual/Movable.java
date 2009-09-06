package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.Node;

public interface Movable extends Node {
	public AffineTransform getTransform();
	public void applyTransform(AffineTransform transform);
}