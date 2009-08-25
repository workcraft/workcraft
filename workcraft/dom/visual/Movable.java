package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.HierarchyNode;

public interface Movable extends HierarchyNode {
	public AffineTransform getTransform();

	public void setTransform(AffineTransform transform);
	public void applyTransform(AffineTransform transform);

	public double getX();
	public void setX(double X);
	public double getY();
	public void setY(double Y);
	public double getRotation();
	public void setRotation(double rotation);
	public double getScaleX();
	public void setScaleX(double scaleX);
	public double getScaleY();
	public void setScaleY(double scaleY);
}