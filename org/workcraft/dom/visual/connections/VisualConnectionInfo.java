package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.geom.Point2D;

public interface VisualConnectionInfo {
	public Color getColor();
	public double getLineWidth();
	public double getArrowWidth();
	public double getArrowLength();
	public Point2D getSecondCenter();
	public Point2D getFirstCenter();
}