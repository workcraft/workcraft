package org.workcraft.dom.visual.connections;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.w3c.dom.Element;
import org.workcraft.dom.HierarchyNode;

public interface ConnectionGraphic {
	public void update();
	public void updateVisibleRange(double tStart, double tEnd);
	public void draw (Graphics2D g);

	public Point2D getPointOnConnection(double t);
	public Point2D getNearestPointOnConnection(Point2D pt);

	public Collection<HierarchyNode> getControls();

	public Rectangle2D getBoundingBox();

	public void cleanup();
	public void click(Point2D point);

	public boolean touchesRectangle(Rectangle2D rect);

	public void writeToXML(Element element);
	public void readFromXML(Element element, ConnectionInfo parent);
}
