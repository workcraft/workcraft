package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawHelper;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.Geometry;
import org.workcraft.util.XmlUtil;

class Polyline implements ConnectionGraphic, Container, ObservableHierarchy,
StateObserver, HierarchyObserver, SelectionObserver {
	LinkedList<ControlPoint> controlPoints = new LinkedList<ControlPoint>();

	private DefaultGroupImpl groupImpl;
	private VisualConnectionInfo connectionInfo;
	private PartialCurveInfo curveInfo;

	private Rectangle2D boundingBox = null;

	public Polyline(VisualConnection parent) {
		groupImpl = new DefaultGroupImpl(this);
		groupImpl.setParent(parent);
		groupImpl.addObserver((HierarchyObserver)this);
		connectionInfo = parent;
	}

	public void draw(Graphics2D g) {
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

		g.setColor(connectionInfo.getDrawColor());
		g.setStroke(new BasicStroke((float)connectionInfo.getLineWidth()));
		g.draw(connectionPath);

		DrawHelper.drawArrowHead(g, connectionInfo.getDrawColor(), curveInfo.arrowHeadPosition, curveInfo.arrowOrientation,
				connectionInfo.getArrowLength(), connectionInfo.getArrowWidth());
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	public void update() {
		int segments = getSegmentCount();

		for (int i=0; i < segments; i++) {
			Line2D seg = getSegment(i);

			if (i==0)
				boundingBox = getSegmentBoundsWithThreshold(seg);
			else
				boundingBox.add(getSegmentBoundsWithThreshold(seg));
		}

		curveInfo = Geometry.buildConnectionCurveInfo(connectionInfo, this, 0);
	}

	private int getSegmentIndex(double t) {
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


	private int getNearestSegment (Point2D pt, Point2D pointOnSegment) {
		double min = Double.MAX_VALUE;
		int nearest = -1;

		for (int i=0; i<getSegmentCount(); i++) {
			Line2D segment = getSegment(i);

			Point2D a = new Point2D.Double ( pt.getX() - segment.getX1(), pt.getY() - segment.getY1() );
			Point2D b = new Point2D.Double ( segment.getX2() - segment.getX1(), segment.getY2() - segment.getY1() );

			double magB = b.distance(0, 0);
			b.setLocation(b.getX() / magB, b.getY() / magB);

			double magAonB = a.getX() * b.getX() + a.getY() * b.getY();

			if (magAonB < 0)
				magAonB = 0;
			if (magAonB > magB)
				magAonB = magB;

			a.setLocation(segment.getX1() + b.getX() * magAonB, segment.getY1() + b.getY() * magAonB);

			double dist = new Point2D.Double(pt.getX() - a.getX(), pt.getY() - a.getY()).distance(0,0);

			if (dist < min) {
				min = dist;
				if (pointOnSegment != null)
					pointOnSegment.setLocation(a);
				nearest = i;
			}
		}

		return nearest;
	}

	private int getSegmentCount() {
		return controlPoints.size() + 1;
	}

	private Point2D getAnchorPointLocation(int index) {
		if (index == 0)
			return connectionInfo.getFirstCenter();
		if (index > controlPoints.size())
			return connectionInfo.getSecondCenter();
		return controlPoints.get(index-1).getPosition();
	}

	private Line2D getSegment(int index) {
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

	public ControlPoint addAnchorPoint(Point2D userLocation) {
		Point2D pointOnConnection = new Point2D.Double();
		int nearestSegment = getNearestSegment(userLocation, pointOnConnection);

		//System.out.println ("nearestSegment = " + nearestSegment);

		ControlPoint ap = new ControlPoint();
		ap.setPosition(pointOnConnection);

		controlPoints.add(nearestSegment, ap);
		add(ap);
		ap.addObserver(this);

		update();

		return ap;
	}

	public void readFromXML(Element element) {
		Element anchors;
		anchors = XmlUtil.getChildElement("anchorPoints", element);
		if (anchors==null) return;
		List<Element> xap = XmlUtil.getChildElements(ControlPoint.class.getSimpleName(), anchors);
		if (xap==null) return;

		ControlPoint pap;
		controlPoints.clear();
		for (Element eap: xap) {
			pap = new ControlPoint();
			pap.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
			pap.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));

			controlPoints.add(pap);
		}
	}

	public void writeToXML(Element element) {
		if (controlPoints.size()>0) {
			Element anchors = XmlUtil.createChildElement("anchorPoints", element);
			Element xap;
			for (ControlPoint ap: controlPoints) {
				xap = XmlUtil.createChildElement(ControlPoint.class.getSimpleName(), anchors);
				XmlUtil.writeDoubleAttr(xap, "X", ap.getX());
				XmlUtil.writeDoubleAttr(xap, "Y", ap.getY());
			}
		}
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

	public void addObserver(StateObserver obs) {
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

	public void removeObserver(StateObserver obs) {
		groupImpl.removeObserver(obs);
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
		update();
	}

	@Override
	public void notify(HierarchyEvent e) {
		if (e instanceof NodesDeletingEvent)
			for (Node n : e.getAffectedNodes())
				if (n instanceof ControlPoint) {
					ControlPoint cp = (ControlPoint)n;
					cp.removeObserver(this);
					controlPoints.remove(cp);
				}
		update();
	}

	@Override
	public void notify(SelectionChangedEvent event) {
		//System.out.println ("Selection changed");
		boolean controlsVisible = false;
		for (Node n : event.getSelection())
			if (n == getParent() || controlPoints.contains(n)) {
				controlsVisible = true;
				break;
			}

		/*if (controlsVisible)
			System.out.println ("Showing controls");
		else
			System.out.println ("Hiding controls");*/

		for (ControlPoint cp : controlPoints)
			cp.setHidden(!controlsVisible);
	}
}