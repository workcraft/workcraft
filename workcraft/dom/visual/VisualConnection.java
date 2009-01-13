package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.gui.Coloriser;

public class VisualConnection extends VisualNode {
	protected Connection refConnection;
	protected VisualComponent first;
	protected VisualComponent second;

	protected double width = 0.02;
	protected double arrowWidth = 0.2;
	protected double arrowLength = 0.4;

	protected Color defaultColor = Color.BLUE;
	protected Color userColor = defaultColor;

	public VisualConnection(Connection refConnection, VisualComponent first, VisualComponent second, VisualComponentGroup parent) {
		super(parent);
		this.refConnection = refConnection;
		this.first = first;
		this.second = second;
	}

	public void draw(Graphics2D g) {
		Line2D line = new Line2D.Double(first.getX(), first.getY(), second.getX(), second.getY());
		g.setColor(Coloriser.colorise(userColor, colorisation));
		g.setStroke(new BasicStroke((float)width));
		g.draw(line);

		// TODO: translate to proper point
		g.translate((second.getX() + first.getX()) * 0.5, (second.getY() + first.getY()) * 0.5);
		g.rotate(Math.atan2(second.getY() - first.getY(), second.getX() - first.getX()));

		Path2D.Double arrowShape = new Path2D.Double();
		arrowShape.moveTo(-arrowLength, -arrowWidth / 2);
		arrowShape.lineTo(-arrowLength, arrowWidth / 2);
		arrowShape.lineTo(0,0);
		arrowShape.closePath();

		g.fill(arrowShape);
	}

	public void toXML(Element vconElement) {

	}

	public Connection getReferencedConnection() {
		return refConnection;
	}

	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return 0;
	}

	public int hitTestInParentSpace(Point2D pointInParentSpace) {
		return 0;
	}

	public int hitTestInUserSpace(Point2D pointInUserSpace) {
		// TODO: detect hits
		return 0;
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return null;
	}

	public Rectangle2D getBoundingBoxInParentSpace() {
		return null;
	}

	public Rectangle2D getBoundingBoxInUserSpace() {
		Rectangle2D bb1 = first.getBoundingBoxInUserSpace();
		Rectangle2D ret = new Rectangle2D.Double(bb1.getMinX(), bb1.getMinY(), bb1.getWidth(), bb1.getHeight());
		ret.add(second.getBoundingBoxInUserSpace());
		return ret;
	}

	public void setColorisation (Color color) {
		colorisation = color;
	}

	public Color getColorisation (Color color) {
		return colorisation;
	}

	public void clearColorisation() {
		setColorisation(null);
	}
}