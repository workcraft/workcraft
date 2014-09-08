/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.ArbitraryInsertionGroupImpl;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;

public class Polyline implements ConnectionGraphic, Container, ObservableHierarchy,
StateObserver, HierarchyObserver, SelectionObserver {
	private ArbitraryInsertionGroupImpl groupImpl;
	protected VisualConnectionProperties connectionInfo;
	protected PartialCurveInfo curveInfo;

	protected Rectangle2D boundingBox = null;

	protected boolean valid = false;
	private ControlPointScaler scaler = null;

	public Polyline(VisualConnection parent) {
		groupImpl = new ArbitraryInsertionGroupImpl(this);
		groupImpl.setParent(parent);
		groupImpl.addObserver((HierarchyObserver)this);
		connectionInfo = parent;
	}

	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		if (!valid) {
			update();
		}

		Path2D connectionPath = new Path2D.Double();

		int start = getSegmentIndex(curveInfo.tStart);
		int end = getSegmentIndex(curveInfo.tEnd);

		Point2D startPt = getPointOnCurve(curveInfo.tStart);
		Point2D endPt = getPointOnCurve(curveInfo.tEnd);

		connectionPath.moveTo(startPt.getX(), startPt.getY());

		for (int i=start; i<end; i++) {
			Line2D segment = getSegment(i);
			connectionPath.lineTo(segment.getX2(), segment.getY2());
		}

		connectionPath.lineTo(endPt.getX(), endPt.getY());

		Color color = Coloriser.colorise(connectionInfo.getDrawColor(), r.getDecoration().getColorisation());
		g.setColor(color);

		g.setStroke(connectionInfo.getStroke());
		g.draw(connectionPath);

		if(connectionInfo.hasArrow()) {
			DrawHelper.drawArrowHead(g, curveInfo.headPosition,	curveInfo.headOrientation,
					connectionInfo.getArrowLength(), connectionInfo.getArrowWidth(), color);
		}

		if (connectionInfo.hasBubble()) {
			DrawHelper.drawBubbleHead(g, curveInfo.headPosition, curveInfo.headOrientation,
					connectionInfo.getBubbleSize(),	color, connectionInfo.getStroke());
		}
	}

	public Rectangle2D getBoundingBox() {
		if (!valid) {
			update();
		}
		return boundingBox;
	}

	public void update() {
		int segments = getSegmentCount();
		for (int i=0; i < segments; i++) {
			Line2D seg = getSegment(i);
			if (i==0) {
				boundingBox = getSegmentBoundsWithThreshold(seg);
			} else {
				boundingBox.add(getSegmentBoundsWithThreshold(seg));
			}
		}
		curveInfo = Geometry.buildConnectionCurveInfo(connectionInfo, this, 0);
		valid = true;
	}

	protected int getSegmentIndex(double t) {
		int segments = getSegmentCount();
		double l = 1.0 / segments;
		double t_l = t/l;

		int n = (int)Math.floor(t_l);
		if (n==segments) n -= 1;
		return n;
	}

	private double getParameterOnSegment (double t, int segmentIndex) {
		return t * getSegmentCount() - segmentIndex;
	}


	private int getNearestSegment (Point2D pt, Point2D out_pointOnSegment) {
		double min = Double.MAX_VALUE;
		int nearest = -1;

		for (int i=0; i<getSegmentCount(); i++) {
			Line2D segment = getSegment(i);

			Point2D a = new Point2D.Double ( pt.getX() - segment.getX1(), pt.getY() - segment.getY1() );
			Point2D b = new Point2D.Double ( segment.getX2() - segment.getX1(), segment.getY2() - segment.getY1() );

			double magB = b.distance(0, 0);

			double dist;

			if (magB < 0.0000001) {
				dist = pt.distance(segment.getP1());
			} else {
				b.setLocation(b.getX() / magB, b.getY() / magB);

				double magAonB = a.getX() * b.getX() + a.getY() * b.getY();

				if (magAonB < 0)
					magAonB = 0;
				if (magAonB > magB)
					magAonB = magB;

				a.setLocation(segment.getX1() + b.getX() * magAonB, segment.getY1() + b.getY() * magAonB);

				dist = new Point2D.Double(pt.getX() - a.getX(), pt.getY() - a.getY()).distance(0,0);
			}

			if (dist < min) {
				min = dist;
				if (out_pointOnSegment != null)
					out_pointOnSegment.setLocation(a);
				nearest = i;
			}
		}

		return nearest;
	}

	protected int getSegmentCount() {
		return groupImpl.getChildren().size() + 1;
	}

	private Point2D getAnchorPointLocation(int index) {
		if (index == 0)
			return connectionInfo.getFirstCenter();
		if (index > groupImpl.getChildren().size())
			return connectionInfo.getSecondCenter();
		return ((ControlPoint) groupImpl.getChildren().get(index-1)).getPosition();
	}

	protected Line2D getSegment(int index) {
		int segments = getSegmentCount();

		if (index > segments-1)
			throw new RuntimeException ("Segment index is greater than number of segments");

		return new Line2D.Double(getAnchorPointLocation(index), getAnchorPointLocation(index+1));
	}

	private Rectangle2D getSegmentBoundsWithThreshold (Line2D segment) {
		Point2D pt1 = segment.getP1();
		Point2D pt2 = segment.getP2();

		Rectangle2D bb = new Rectangle2D.Double(pt1.getX(), pt1.getY(), 0, 0);
		bb.add(pt2);
		Point2D lineVec = new Point2D.Double(pt2.getX() - pt1.getX(), pt2.getY() - pt1.getY());

		double mag = lineVec.distance(0, 0);

		if (mag==0)
			return bb;

		lineVec.setLocation(lineVec.getY()*VisualConnection.HIT_THRESHOLD/mag, -lineVec.getX()*VisualConnection.HIT_THRESHOLD/mag);
		bb.add(pt1.getX() + lineVec.getX(), pt1.getY() + lineVec.getY());
		bb.add(pt2.getX() + lineVec.getX(), pt2.getY() + lineVec.getY());
		bb.add(pt1.getX() - lineVec.getX(), pt1.getY() - lineVec.getY());
		bb.add(pt2.getX() - lineVec.getX(), pt2.getY() - lineVec.getY());

		return bb;
	}

	public void createControlPoint(Point2D userLocation) {
		Point2D pointOnConnection = new Point2D.Double();
		int segment = getNearestSegment(userLocation, pointOnConnection);

		ControlPoint ap = new ControlPoint();
		ap.setPosition(pointOnConnection);
		ap.setHidden(false);

		groupImpl.add(segment, ap);

		controlPointsChanged();
	}

	@Override
	public boolean hitTest(Point2D point) {
		return getDistanceToCurve(point) < VisualConnection.HIT_THRESHOLD;
	}

	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	public Node getParent() {
		return groupImpl.getParent();
	}

	public void setParent(Node parent) {
		throw new RuntimeException ("Node does not support reparenting");
	}

	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	public void add(Node node) {
		groupImpl.add(node);
	}

	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	public void remove(Collection<Node> nodes) {
		groupImpl.remove(nodes);
	}

	public void remove(Node node) {
		groupImpl.remove(node);
	}

	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	public void removeAllObservers() {
		groupImpl.removeAllObservers();
	}

	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public Point2D getNearestPointOnCurve(Point2D pt) {
		Point2D result = new Point2D.Double();
		getNearestSegment(pt, result);
		return result;
	}

	@Override
	public Point2D getPointOnCurve(double t) {
		int segmentIndex = getSegmentIndex(t);
		double t2 = getParameterOnSegment(t, segmentIndex);

		Line2D segment = getSegment(segmentIndex);

		return new Point2D.Double(segment.getP1().getX() * (1-t2) + segment.getP2().getX() * t2,
				segment.getP1().getY() * (1-t2) + segment.getP2().getY() * t2);
	}

	@Override
	public double getDistanceToCurve(Point2D pt) {
		double min = Double.MAX_VALUE;
		for (int i=0; i<getSegmentCount(); i++) {
			Line2D segment = getSegment(i);
			double dist = segment.ptSegDist(pt);
			if (dist < min)
				min = dist;
		}
		return min;
	}

	@Override
	public void notify(StateEvent e) {
		controlPointsChanged();
	}

	@Override
	public void notify(HierarchyEvent e) {
		if (e instanceof NodesDeletingEvent)
			for (Node n : e.getAffectedNodes())
				if (n instanceof ControlPoint) {
					ControlPoint cp = (ControlPoint)n;
					cp.removeObserver(this);
			}
		if (e instanceof NodesAddedEvent)
			for (Node n : e.getAffectedNodes())
				if (n instanceof ControlPoint) {
					ControlPoint cp = (ControlPoint)n;
					cp.addObserver(this);
				}

		controlPointsChanged();
	}

	@Override
	public void notify(SelectionChangedEvent event) {
		boolean controlsVisible = false;
		for (Node n : event.getSelection())
			if (n == getParent() || groupImpl.getChildren().contains(n)) {
				controlsVisible = true;
				break;
			}

		for (Node n : groupImpl.getChildren())
			((ControlPoint)n).setHidden(!controlsVisible);
	}

	@Override
	public void componentsTransformChanged() {
		if(scaler == null) {
			System.err.print("error @ Polyline.componentsTransformChanged(): scaler == null");
		} else {
			scaler.scale(connectionInfo.getFirstCenter(), connectionInfo
				.getSecondCenter(), Hierarchy.filterNodesByType(getChildren(),
				ControlPoint.class), connectionInfo.getScaleMode());
			rememberScale();
		}

		invalidate();
	}

	@Override
	public void componentsTransformChanging() {
		if(scaler == null)
			rememberScale();
	}

	private void rememberScale() {
		scaler = new ControlPointScaler(connectionInfo.getFirstCenter(), connectionInfo.getSecondCenter());
	}

	@Override
	public void controlPointsChanged() {
		invalidate();
	}

	@Override
	public void invalidate() {
		valid = false;
	}

	@Override
	public Point2D getDerivativeAt(double t) {
		if (t < 0) t = 0;
		if (t > 1) t = 1;

		int segmentIndex = getSegmentIndex(t);
		Line2D segment = getSegment(segmentIndex);

		return Geometry.subtract(segment.getP2(), segment.getP1());
	}

	@Override
	public Point2D getSecondDerivativeAt(double t) 	{
		Point2D left = getDerivativeAt(t - 0.05);
		Point2D right = getDerivativeAt(t + 0.05);

		return Geometry.subtract(right, left);
	}

	@Override
	public Point2D getCenter() {
		return getPointOnCurve(0.5);
	}

}