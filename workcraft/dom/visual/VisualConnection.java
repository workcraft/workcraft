package org.workcraft.dom.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.XmlUtil;



public class VisualConnection extends VisualNode implements PropertyChangeListener  {
	public enum ConnectionType
	{
		POLYLINE,
		BEZIER
	};

	interface ConnectionImplementation {
		public void update();
		public void draw (Graphics2D g);

		public Point2D getPointOnConnection(double t);
		public Point2D getNearestPointOnConnection(Point2D pt);
		public double getDistanceToConnection(Point2D pt);

		public VisualConnectionAnchorPoint addAnchorPoint(Point2D pt);
//		public void removeAnchorPoint(int index);
		public void removeAnchorPoint(VisualConnectionAnchorPoint anchor);
		public void removeAllAnchorPoints();

		public Rectangle2D getBoundingBox();

		public int getAnchorPointCount();
		public VisualConnectionAnchorPoint[] getAnchorPointComponents();

		public boolean touchesRectangle(Rectangle2D rect);

		public void writeToXML(Element element);
		public void readFromXML(Element element, VisualConnection parent);
	}

	class Polyline implements ConnectionImplementation {

		class PolylineAnchorPoint extends VisualConnectionAnchorPoint {

			private int index;
			private double size = 0.25;
			private Color fillColor = Color.BLUE.darker();

			Shape shape = new Rectangle2D.Double(
					-size / 2,
					-size / 2,
					size,
					size);

			public void removeAnchorPoint(VisualConnectionAnchorPoint anchor) {
				anchorPoints.remove(anchor);
			}


			public PolylineAnchorPoint(VisualConnection parent) {
				super(parent);

			}

//			public PolylineAnchorPoint(int index) {
//				this.index = index;
//			}

			public Rectangle2D getBoundingBoxInLocalSpace() {
				return new Rectangle2D.Double(-size/2, -size/2, size, size);
			}

			public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
				if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
					return 1;
				else
					return 0;
			}

			@Override
			protected void drawInLocalSpace(Graphics2D g) {
				g.setColor(Coloriser.colorise(fillColor, getColorisation()));
				g.fill(shape);
			}

			public int getIndex() {
				return index;
			}
		}

		private ArrayList<PolylineAnchorPoint> anchorPoints = new ArrayList<PolylineAnchorPoint>();

		private double[] polylineSegmentLengthsSq;
		private double polylineLengthSq;
		private Rectangle2D boundingBox = null;

		public Polyline() {
		}

		public void draw(Graphics2D g) {
			Path2D connectionPath = new Path2D.Double();
			connectionPath.moveTo(firstCenter.getX(), firstCenter.getY());
			for (PolylineAnchorPoint pt : anchorPoints) {
				connectionPath.lineTo(pt.getX(), pt.getY());
			}
			connectionPath.lineTo(secondCenter.getX(), secondCenter.getY());
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
			double targetOffsetSq = t * polylineLengthSq;
			double currentOffsetSq = 0;

			int segments = getSegmentCount();

			for (int i=0; i <segments; i++) {
				if (currentOffsetSq + polylineSegmentLengthsSq[i] > targetOffsetSq) {
					double t2 = (targetOffsetSq - currentOffsetSq) / polylineSegmentLengthsSq[i];
					Line2D segment = getSegment(i);
					return new Point2D.Double(segment.getP1().getX() * (1-t2) + segment.getP2().getX() * t2,
							segment.getP1().getY() * (1-t2) + segment.getP2().getY() * t2);
				}
				currentOffsetSq += polylineSegmentLengthsSq[i];
			}

			return secondCenter;
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
				return firstCenter;
			if (index > anchorPoints.size())
				return secondCenter;
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

			lineVec.setLocation(lineVec.getY()*hitThreshold/mag, -lineVec.getX()*hitThreshold/mag);
			bb.add(pt1.getX() + lineVec.getX(), pt1.getY() + lineVec.getY());
			bb.add(pt2.getX() + lineVec.getX(), pt2.getY() + lineVec.getY());
			bb.add(pt1.getX() - lineVec.getX(), pt1.getY() - lineVec.getY());
			bb.add(pt2.getX() - lineVec.getX(), pt2.getY() - lineVec.getY());

			return bb;
		}

		public VisualConnectionAnchorPoint addAnchorPoint(Point2D userLocation) {
			Point2D pointOnConnection = new Point2D.Double();
			int nearestSegment = getNearestSegment(userLocation, pointOnConnection);

//			PolylineAnchorPoint ap = new PolylineAnchorPoint(nearestSegment);
			PolylineAnchorPoint ap = new PolylineAnchorPoint(VisualConnection.this);

			ap.setPosition(pointOnConnection);

			ap.addListener(new PropertyChangeListener() {
				public void onPropertyChanged(String propertyName, Object sender) {
					VisualConnection.this.update();
				}
			});
			if (anchorPoints.size() == 0)
				anchorPoints.add(ap);
			else
				anchorPoints.add(nearestSegment, ap);

			VisualConnection.this.update();

			firePropertyChanged("anchors");
			return ap;
		}

//		public void removeAnchorPoint (int index) {
//
//			anchorPoints.remove(index);
//			VisualConnection.this.update();
//			firePropertyChanged("anchors");
//		}

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
			for (VisualConnectionAnchorPoint ap: anchorPoints) {
				ap.getParentConnection().getParent().remove(ap);
			}

			anchorPoints.clear();
			VisualConnection.this.update();
			firePropertyChanged("anchors");
		}

		public void removeAnchorPoint(VisualConnectionAnchorPoint anchor) {
			anchor.getParentConnection().getParent().remove(anchor);

			anchorPoints.remove(anchor);

			VisualConnection.this.update();
			firePropertyChanged("anchors");
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

		public void readFromXML(Element element, VisualConnection parent) {
			Element anchors;
			anchors = XmlUtil.getChildElement("anchorPoints", element);
			if (anchors==null) return;
			List<Element> xap = XmlUtil.getChildElements(VisualConnectionAnchorPoint.class.getSimpleName(), anchors);
			if (xap==null) return;

			PolylineAnchorPoint pap;
			anchorPoints.clear();
			for (Element eap: xap) {
				pap = new PolylineAnchorPoint(parent);
				pap.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
				pap.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));

				pap.addListener(new PropertyChangeListener() {
					public void onPropertyChanged(String propertyName, Object sender) {
						VisualConnection.this.update();
					}
				});
				anchorPoints.add(pap);

				parent.update();
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

	}

	protected Connection refConnection;
	protected VisualComponent first;
	protected VisualComponent second;

	private ConnectionType connectionType = ConnectionType.POLYLINE;

	private Point2D firstCenter = new Point2D.Double();
	private Point2D secondCenter = new Point2D.Double();
	private Point2D arrowHeadPosition = new Point2D.Double();
	private double arrowOrientation = 0;

	private ConnectionImplementation impl = new Polyline();

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	private static double hitThreshold = 0.2;
	private static Color defaultColor = Color.BLACK;

	private Color color = defaultColor;
	private double lineWidth = defaultLineWidth;
	private double arrowWidth = defaultArrowWidth;
	private double arrowLength = defaultArrowLength;

	protected void initialise() {
		first.addListener(this);
		second.addListener(this);

		update();

		addPropertyDeclaration(new PropertyDeclaration("Line width", "getLineWidth", "setLineWidth", double.class));
		addPropertyDeclaration(new PropertyDeclaration("Arrow width", "getArrowWidth", "setArrowWidth", double.class));

		LinkedHashMap<String, Object> arrowLengths = new LinkedHashMap<String, Object>();
		arrowLengths.put("short", 0.2);
		arrowLengths.put("medium", 0.4);
		arrowLengths.put("long", 0.8);

		addPropertyDeclaration(new PropertyDeclaration("Arrow length", "getArrowLength", "setArrowLength", double.class, arrowLengths));

		LinkedHashMap<String, Object> hm = new LinkedHashMap<String, Object>();

		hm.put("Polyline", ConnectionType.POLYLINE);
		hm.put("Bezier", ConnectionType.BEZIER);

		addPropertyDeclaration(new PropertyDeclaration("Connection type", "getConnectionType", "setConnectionType", ConnectionType.class, hm));



		addXMLSerialiser(new XMLSerialiser() {

			public String getTagName() {
				return VisualConnection.class.getSimpleName();
			}

			public void serialise(Element element) {
				if (refConnection != null)
					XmlUtil.writeIntAttr(element, "refID", refConnection.getID());
				writeXMLConnectionProperties(element);
			}
		});
	}

	protected VisualConnection() {

	}


	public VisualConnection(Connection refConnection, VisualComponent first, VisualComponent second) {
		this.refConnection = refConnection;
		this.first = first;
		this.second = second;

		initialise();
	}



	protected void writeXMLConnectionProperties(Element element) {
		XmlUtil.writeDoubleAttr(element, "arrowLength", getArrowLength());
		XmlUtil.writeDoubleAttr(element, "arrowWidth", getArrowWidth());
		XmlUtil.writeDoubleAttr(element, "lineWidth", getLineWidth());
		XmlUtil.writeStringAttr(element, "type", getConnectionType().name());
		impl.writeToXML(element);
	}

	protected void readXMLConnectionProperties(Element element) {
		String strConnectionType = XmlUtil.readStringAttr(element, "type");
		if (!strConnectionType.equals(""))
		connectionType = ConnectionType.valueOf(strConnectionType);
		setArrowLength(XmlUtil.readDoubleAttr(element, "arrowLength", defaultArrowLength));
		setArrowWidth(XmlUtil.readDoubleAttr(element, "arrowWidth", defaultArrowWidth));
		setLineWidth(XmlUtil.readDoubleAttr(element, "lineWidth", defaultLineWidth));
		impl.readFromXML(element, VisualConnection.this);
	}

	public VisualConnection (Element xmlElement, VisualReferenceResolver referenceResolver) {
		Element element = XmlUtil.getChildElement(VisualConnection.class.getSimpleName(), xmlElement);

		refConnection = referenceResolver.getConnectionByID(XmlUtil.readIntAttr(element, "refID", -1));
		first = referenceResolver.getComponentByRefID(refConnection.getFirst().getID());
		second = referenceResolver.getComponentByRefID(refConnection.getSecond().getID());

		readXMLConnectionProperties(element);

		initialise();
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType t) {
		connectionType = t;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		if (lineWidth < 0.01)
			lineWidth = 0.01;
		if (lineWidth > 0.5)
			lineWidth = 0.5;
		this.lineWidth = lineWidth;
	}

	public double getArrowWidth() {
		return arrowWidth;
	}

	public void setArrowWidth(double arrowWidth) {
		if (arrowWidth > 1)
			arrowWidth = 1;
		if (arrowWidth < 0.1)
			arrowWidth = 0.1;
		this.arrowWidth = arrowWidth;
	}

	public double getArrowLength() {
		return arrowLength;
	}

	public void setArrowLength(double arrowLength) {
		if (arrowLength > 1)
			arrowLength = 1;
		if (arrowLength < 0.1)
			arrowLength = 0.1;
		this.arrowLength = arrowLength;
		update();
	}

	public Point2D getPointOnConnection(double t) {
		return impl.getPointOnConnection(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return impl.getNearestPointOnConnection(pt);
	}

	public void removeAllAnchorPoints() {
		impl.removeAllAnchorPoints();
	}

	public void removeAnchorPoint (VisualConnectionAnchorPoint anchor) {
		impl.removeAnchorPoint(anchor);
	}

	public VisualConnectionAnchorPoint addAnchorPoint (Point2D userPoint) {
		return impl.addAnchorPoint(userPoint);
	}


	public void update() {
		if (getParent() == null)
			return;
		AffineTransform t1,t2;
		try {
			t1 = first.getParentToAncestorTransform(getParent());
			t2 = second.getParentToAncestorTransform(getParent());
		} catch (NotAnAncestorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		// get centres of the two components in this connection's parent space
		Rectangle2D firstBB = first.getBoundingBoxInParentSpace();
		Rectangle2D secondBB = second.getBoundingBoxInParentSpace();

		firstCenter.setLocation(firstBB.getCenterX(), firstBB.getCenterY());
		secondCenter.setLocation(secondBB.getCenterX(), secondBB.getCenterY());

		t1.transform(firstCenter, firstCenter);
		t2.transform(secondCenter, secondCenter);

		impl.update();

		// create transforms from this connection's parent space to
		// components' parent spaces, for hit testing
		AffineTransform it2;
		try {
			it2 = t2.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
			return;
		}

		Point2D pt = new Point2D.Double();

		// find arrow head position
		double t = 0.0; double dt = 1.0;

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnConnection(t);
			arrowHeadPosition.setLocation(pt);

			it2.transform(pt, pt);
			if (second.hitTestInParentSpace(pt) != 0)
				t-=dt;
		}

		//  find arrow base position
		dt = t; t = 0.0;

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnConnection(t);
			if (arrowHeadPosition.distanceSq(pt) < arrowLength*arrowLength)
				t-=dt;
		}

		arrowOrientation = Math.atan2(arrowHeadPosition.getY() - pt.getY() , arrowHeadPosition.getX() - pt.getX());
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Coloriser.colorise(color, getColorisation()));
		g.setStroke(new BasicStroke((float)lineWidth));

		Path2D.Double arrowShape = new Path2D.Double();
		arrowShape.moveTo(-arrowLength, -arrowWidth / 2);
		arrowShape.lineTo(-arrowLength, arrowWidth / 2);
		arrowShape.lineTo(0,0);
		arrowShape.closePath();

		Rectangle2D arrowBounds = arrowShape.getBounds2D();
		arrowBounds.setRect(arrowBounds.getMinX()+0.05f, arrowBounds.getMinY(), arrowBounds.getWidth(), arrowBounds.getHeight());

		AffineTransform arrowTransform = new AffineTransform();
		arrowTransform.translate(arrowHeadPosition.getX(), arrowHeadPosition.getY());
		arrowTransform.rotate(arrowOrientation);

		Shape transformedArrowShape = arrowTransform.createTransformedShape(arrowShape);
		Shape transformedArrowBounds = arrowTransform.createTransformedShape(arrowBounds);

		impl.draw(g);

		/*Shape clip = g.getClip();

		if (clip == null)
			throw new RuntimeException ("waazup");

		Area clipArea = new Area(clip);
		clipArea.subtract(new Area(transformedArrowBounds));


		g.setClip(clipArea);*/
		//g.setClip(clip);

		g.fill(transformedArrowShape);
	}

	public Connection getReferencedConnection() {
		return refConnection;
	}
	public double distanceToConnection (Point2D pointInParentSpace) {
		return impl.getDistanceToConnection(pointInParentSpace);
	}

	public int hitTestInParentSpace(Point2D pointInParentSpace) {
		if (distanceToConnection(pointInParentSpace) < hitThreshold)
			return 1;
		else
			return 0;
	}

	@Override
	public boolean touchesRectangle(Rectangle2D rect) {
		return impl.touchesRectangle(rect);
	}

	@Override
	public Rectangle2D getBoundingBoxInParentSpace() {
		return impl.getBoundingBox();
	}

	public void onPropertyChanged(String propertyName, Object sender) {
		if (propertyName.equals("X") || propertyName.equals("Y") || propertyName.equals("transform") || propertyName.equals("shape"));
		update();
	}

	@Override
	public void setParent(VisualGroup parent) {
		super.setParent(parent);
		update();
	}

	public VisualComponent getFirst() {
		return first;
	}

	public VisualComponent getSecond() {
		return second;
	}

	public Set<MathNode> getReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
	}

	public VisualConnectionAnchorPoint[] getAnchorPointComponents() {
		return impl.getAnchorPointComponents();
	}

	public void showAnchorPoints() {
		for (VisualConnectionAnchorPoint ap: impl.getAnchorPointComponents()) {
			ap.getParentConnection().getParent().add(ap);
		}
	}

	public void hideAnchorPoints() {
		for (VisualConnectionAnchorPoint ap: impl.getAnchorPointComponents()) {
			ap.getParentConnection().getParent().remove(ap);
		}
	}

}