package org.workcraft.plugins.balsa;

import java.awt.geom.Point2D;

import org.workcraft.plugins.balsa.VisualBreezeComponent.Direction;

public class Ray
{
	public Ray(double x, double y, Direction direction)
	{
		this.position = new Point2D.Double(x, y);
		this.direction = direction;
	}
	public final Point2D.Double position;
	public final Direction direction;
}
