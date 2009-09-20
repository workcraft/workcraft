package org.workcraft.dom.visual.connections;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;

public interface ConnectionGraphic extends Node, Drawable, Touchable {
	public void update();
	public void updateVisibleRange(double tStart, double tEnd);

	public void draw (Graphics2D g);

	public Point2D getPointOnConnection(double t);
	public Point2D getNearestPointOnConnection(Point2D pt);

	public Rectangle2D getBoundingBox();

	public void writeToXML(Element element);
	public void readFromXML(Element element);
}
