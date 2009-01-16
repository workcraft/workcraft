package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.Coloriser;

public class VisualConnection extends VisualNode implements PropertyChangeListener  {
	protected Connection refConnection;

	protected VisualComponent first;
	protected VisualComponent second;

	protected Point2D firstCenter = new Point2D.Double();
	protected Point2D secondCenter = new Point2D.Double();
	protected Point2D lineStart = new Point2D.Double();
	protected Point2D lineEnd = new Point2D.Double();
	protected Point2D arrowHeadPosition = new Point2D.Double();
	protected double arrowOrientation = 0;

	protected double width = 0.02;
	protected double arrowWidth = 0.2;
	protected double arrowLength = 0.4;
	protected double hitThreshold = 0.2;

	protected Color defaultColor = Color.BLUE;
	protected Color userColor = defaultColor;

	public VisualConnection(Connection refConnection, VisualComponent first, VisualComponent second, VisualComponentGroup parent) {
		super(parent);

		first.addListener(this);

		this.refConnection = refConnection;
		this.first = first;
		this.second = second;

		first.addListener(this);
		second.addListener(this);

		update();
	}

	protected Point2D getPointOnCenterLine (double t) {
		return new Point2D.Double(firstCenter.getX() * (1-t) + secondCenter.getX() * t, firstCenter.getY() * (1-t) + secondCenter.getY() * t);
	}

	protected Point2D getPointOnConnection(double t) {
		return new Point2D.Double(lineStart.getX() * (1-t) + arrowHeadPosition.getX() * t, lineStart.getY() * (1-t) + arrowHeadPosition.getY() * t);
	}

	public void update() {
		AffineTransform t1, t2;
		try {
			t1 = first.getParentToAncestorTransform(parent);
			t2 = second.getParentToAncestorTransform(parent);
		} catch (NotAnAncestorException e) {
			e.printStackTrace();
			return;
		}

		// get centres of the two components in this connection's parent space
		Rectangle2D firstBB = first.getBoundingBoxInParentSpace();
		Rectangle2D secondBB = second.getBoundingBoxInParentSpace();

		firstCenter.setLocation(firstBB.getCenterX(), firstBB.getCenterY());
		secondCenter.setLocation(secondBB.getCenterX(), secondBB.getCenterY());

		t1.transform(firstCenter, firstCenter);
		t2.transform(secondCenter, secondCenter);

		// create transforms from this connection's parent space to
		// components' parent spaces, for hit testing
		AffineTransform it1, it2;
		try {
			it1 = t1.createInverse();
			it2 = t2.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
			return;
		}

		Point2D pt = new Point2D.Double();

		// find connection line starting point
		double t = 1.0, dt = 1.0;

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t -= dt;
			pt = getPointOnCenterLine(t);
			lineStart.setLocation(pt);
			it1.transform(pt, pt);
			if (first.hitTestInParentSpace(pt) != 0)
				t+=dt;
		}

		// find arrowHeadPosition
		t = 0.0; dt = 1.0;

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnCenterLine(t);
			arrowHeadPosition.setLocation(pt);

			it2.transform(pt, pt);
			if (second.hitTestInParentSpace(pt) != 0)
				t-=dt;
		}

		//  find connection line ending point
		t = 0.0; dt = 1.0;
		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnCenterLine(t);
			if (arrowHeadPosition.distanceSq(pt) < arrowLength*arrowLength)
				t-=dt;
		}

		lineEnd = pt;
		arrowOrientation = Math.atan2(arrowHeadPosition.getY() - lineEnd.getY() , arrowHeadPosition.getX() - lineEnd.getX());
	}

	public void draw(Graphics2D g) {
		g.setColor(Coloriser.colorise(userColor, colorisation));
		g.setStroke(new BasicStroke((float)width));

		Line2D line = new Line2D.Double(lineStart, lineEnd);
		g.draw(line);

		g.translate(arrowHeadPosition.getX(), arrowHeadPosition.getY());
		g.rotate(arrowOrientation);

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

	private double distanceToConnection (Point2D pointInParentSpace) {
		Point2D v = new Point2D.Double();
		v.setLocation(arrowHeadPosition.getX() - lineStart.getX(), arrowHeadPosition.getY() - lineStart.getY());
		Point2D vv = new Point2D.Double();
		vv.setLocation(pointInParentSpace.getX() - lineStart.getX(), pointInParentSpace.getY() - lineStart.getY());
		Point2D pt = new Point2D.Double();
		pt.setLocation(pointInParentSpace);

		double c1 = v.getX() * vv.getX() + v.getY() * vv.getY();
		if(c1<=0) {
			pt.setLocation(pt.getX() - lineStart.getX(), pt.getY() - lineStart.getY());
			return pt.distance(0, 0);
		}

		double c2 = v.getX() * v.getX() + v.getY() * v.getY();

		if(c2<=c1) {
			pt.setLocation(pt.getX() - arrowHeadPosition.getX(), pt.getY() - arrowHeadPosition.getY());
			return pt.distance(0, 0);
		}

		double b = c1/c2;

		pt.setLocation(pt.getX() - v.getX() *b - lineStart.getX(), pt.getY() - v.getY() * b - lineStart.getY());
		return pt.distance(0, 0);
	}

	public int hitTestInParentSpace(Point2D pointInParentSpace) {
		if (distanceToConnection(pointInParentSpace) < arrowWidth)
			return 1;
		else
			return 0;
	}

	public Rectangle2D getBoundingBoxInParentSpace() {
		Rectangle2D bb = new Rectangle2D.Double(lineStart.getX(), lineStart.getY(), 0, 0);
		bb.add(arrowHeadPosition);
		return bb;
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

	public void propertyChanged(String propertyName, Object sender) {
		if (propertyName.equals("transform"))
			update();
	}
}