package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.Touchable;

public interface VisualConnectionInfo {
	public Color getDrawColor();
	public double getLineWidth();
	public double getArrowWidth();
	public double getArrowLength();
	public Point2D getSecondCenter();
	public Point2D getFirstCenter();
	public Touchable getFirstShape();
	public Touchable getSecondShape();
}