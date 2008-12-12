package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;

public class VisualConnection implements Selectable {
	Connection refConnection;
	VisualComponent first;
	VisualComponent second;


	public VisualConnection(Connection refConnection, VisualComponent first, VisualComponent second) {
		this.refConnection = refConnection;
		this.first = first;
		this.second = second;
	}

	public void draw(Graphics2D g) {
		Line2D line = new Line2D.Double(first.getX(), first.getY(), second.getX(), second.getY());
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(0.1f));
		g.draw(line);

	}

	public void toXML(Element vconElement) {

	}

	public Connection getReferencedConnection() {
		return this.refConnection;
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return false;
	}

	public boolean hitTestInParentSpace(Point2D pointInParentSpace) {
		return false;
	}

	public boolean hitTestInUserSpace(Point2D pointInUserSpace) {
		return false;
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return null;
	}

	public Rectangle2D getBoundingBoxInParentSpace() {
		return null;
	}

	@Override
	public Rectangle2D getBoundingBoxInUserSpace() {
		return null;
	}
}
