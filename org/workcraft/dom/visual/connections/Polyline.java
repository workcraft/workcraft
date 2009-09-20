package org.workcraft.dom.visual.connections;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.StateObserver;
import org.workcraft.util.XmlUtil;

class Polyline implements ConnectionGraphic, Container {
	ArrayList<PolylineAnchorPoint> anchorPoints = new ArrayList<PolylineAnchorPoint>();

	private DefaultGroupImpl groupImpl;
	private VisualConnectionInfo connectionInfo;

	private double tStart = 0.0, tEnd = 1.0;

	private double[] polylineSegmentLengthsSq;
	private double polylineLengthSq;
	private Rectangle2D boundingBox = null;

	public Polyline(VisualConnection parent) {
		groupImpl = new DefaultGroupImpl(this);
		groupImpl.setParent(parent);

		connectionInfo = parent;
	}

	public void draw(Graphics2D g) {
		Path2D connectionPath = new Path2D.Double();

		//System.out.println ("tStart = " + tStart + ", tEnd = " + tEnd);

		double toSkipSq = polylineLengthSq * tStart * tStart;
		double toCoverSq = polylineLengthSq * tEnd * tEnd - toSkipSq;
		double skippedSq = 0;

		if (toCoverSq <= 0)
			return;

		int segment = 0;

		while(segment < getSegmentCount()) {
		//	System.out.println ("Segment: " + segment);

			if (toSkipSq < polylineSegmentLengthsSq[segment])
			{
				double part = Math.sqrt(toSkipSq / polylineSegmentLengthsSq[segment]);
			//	System.out.println ("Skipping " + part);
				Line2D seg = getSegment(segment);
				connectionPath.moveTo(seg.getX1() + (seg.getX2()-seg.getX1()) * part,
										seg.getY1() + (seg.getY2()-seg.getY1()) * part);

				double lengthLeftSq = polylineSegmentLengthsSq[segment] - toSkipSq;

			//	System.out.println ("Length left " + lengthLeftSq + ", to cover " + toCoverSq);

				if (lengthLeftSq < toCoverSq) {
					connectionPath.lineTo(seg.getX2(), seg.getY2());
					toCoverSq -= lengthLeftSq;
					segment++;
				} else {
					skippedSq = toSkipSq;
				}

				break;
			} else
			{
		//		System.out.println ("Skipping segment " + segment);
				toSkipSq -= polylineSegmentLengthsSq[segment];
				segment++;
			}
		}

	//	System.out.println ("------");

		while (segment < getSegmentCount()) {
			//System.out.println ("Segment: " + segment);
			if (toCoverSq > polylineSegmentLengthsSq[segment]) {
				//System.out.println ("Drawing segment " + segment);
				toCoverSq -= polylineSegmentLengthsSq[segment];
				segment++;
				connectionPath.lineTo(getSegment(segment).getX2(), getSegment(segment).getY2());
			} else {
				double part = Math.sqrt ((toCoverSq + skippedSq) / polylineSegmentLengthsSq[segment]);

				//System.out.println ("Drawing " + part);

				Line2D seg = getSegment(segment);
				connectionPath.lineTo(seg.getX1() + (seg.getX2()-seg.getX1()) * part,
										seg.getY1() + (seg.getY2()-seg.getY1()) * part);

				break;
			}
		}

	//	System.out.println ("======");

		g.draw(connectionPath);
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}

	public void update() {

		int segments = getSegmentCount();
		polylineSegmentLengthsSq = new double[segments];
		polylineLengthSq = 0;

		for (int i=0; i < segments; i++) {
			Line2D seg = getSegment(i);

			if (i==0)
				boundingBox = getSegmentBoundsWithThreshold(seg);
			else
				boundingBox.add(getSegmentBoundsWithThreshold(seg));

			double a = seg.getP2().getX() - seg.getP1().getX();
			double b = seg.getP2().getY() - seg.getP1().getY();
			double lensq = a*a + b*b;
			polylineSegmentLengthsSq[i] = lensq;
			polylineLengthSq += lensq;
		}
	}

	public Point2D getPointOnConnection(double t) {
		double targetOffsetSq = t * t * polylineLengthSq;
		double currentOffsetSq = 0;

		int segments = getSegmentCount();

		for (int i=0; i <segments; i++) {
			if (currentOffsetSq + polylineSegmentLengthsSq[i] > targetOffsetSq) {
				double t2 = Math.sqrt((targetOffsetSq - currentOffsetSq) / polylineSegmentLengthsSq[i]);
				Line2D segment = getSegment(i);
				return new Point2D.Double(segment.getP1().getX() * (1-t2) + segment.getP2().getX() * t2,
						segment.getP1().getY() * (1-t2) + segment.getP2().getY() * t2);
			}
			currentOffsetSq += polylineSegmentLengthsSq[i];
		}

		return connectionInfo.getSecondCenter();
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		Point2D result = new Point2D.Double();
		getNearestSegment(pt, result);
		return result;
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
		return anchorPoints.size() + 1;
	}

	private Point2D getAnchorPointLocation(int index) {
		if (index == 0)
			return connectionInfo.getFirstCenter();
		if (index > anchorPoints.size())
			return connectionInfo.getSecondCenter();
		return anchorPoints.get(index-1).getPosition();
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

	public VisualConnectionAnchorPoint addAnchorPoint(Point2D userLocation) {
		Point2D pointOnConnection = new Point2D.Double();
		int nearestSegment = getNearestSegment(userLocation, pointOnConnection);

		PolylineAnchorPoint ap = new PolylineAnchorPoint(this);
		ap.setPosition(pointOnConnection);

		if (anchorPoints.size() == 0)
			anchorPoints.add(ap);
		else
			anchorPoints.add(nearestSegment, ap);

		add(ap);

		return ap;
	}

//	public void removeAnchorPoint (int index) {
//
//		anchorPoints.remove(index);
//		VisualConnection.this.update();
//		firePropertyChanged("anchors");
//	}

	public double getDistanceToConnection(Point2D pt) {
		double min = Double.MAX_VALUE;
		for (int i=0; i<getSegmentCount(); i++) {
			Line2D segment = getSegment(i);
			double dist = segment.ptSegDist(pt);
			if (dist < min)
				min = dist;
		}
		return min;
	}

	public VisualConnectionAnchorPoint[] getAnchorPointComponents() {
		return anchorPoints.toArray(new VisualConnectionAnchorPoint[0]);
	}


	public void removeAllAnchorPoints() {
		anchorPoints.clear();
	}

	public void removeAnchorPoint(VisualConnectionAnchorPoint anchor) {
		anchorPoints.remove(anchor);
	}

	public boolean touchesRectangle(Rectangle2D rect) {
		if (!rect.intersects(getBoundingBox())) return false;

		for (VisualConnectionAnchorPoint ap: anchorPoints) {
			if (rect.contains(ap.getPosition())) return true;
		}

		for (int i=0;i<getSegmentCount();i++) {
			if (rect.intersectsLine(getSegment(i))) {
				return true;
			}
		}

		return false;
	}

	public int getAnchorPointCount() {
		return anchorPoints.size();
	}

	public void readFromXML(Element element) {
		Element anchors;
		anchors = XmlUtil.getChildElement("anchorPoints", element);
		if (anchors==null) return;
		List<Element> xap = XmlUtil.getChildElements(VisualConnectionAnchorPoint.class.getSimpleName(), anchors);
		if (xap==null) return;

		PolylineAnchorPoint pap;
		anchorPoints.clear();
		for (Element eap: xap) {
			pap = new PolylineAnchorPoint(this);
			pap.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
			pap.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));

			anchorPoints.add(pap);
		}
	}

	public void writeToXML(Element element) {
		if (anchorPoints.size()>0) {
			Element anchors = XmlUtil.createChildElement("anchorPoints", element);
			Element xap;
			for (VisualConnectionAnchorPoint ap: anchorPoints) {
				xap = XmlUtil.createChildElement(VisualConnectionAnchorPoint.class.getSimpleName(), anchors);
				XmlUtil.writeDoubleAttr(xap, "X", ap.getX());
				XmlUtil.writeDoubleAttr(xap, "Y", ap.getY());
			}
		}
	}

	public void updateVisibleRange(double start, double end) {
		tStart = start;
		tEnd = end;
	}

	@Override
	public boolean hitTest(Point2D point) {
		return getDistanceToConnection(point) < VisualConnection.HIT_THRESHOLD;
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

}
