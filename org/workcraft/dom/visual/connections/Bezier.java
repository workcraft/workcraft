package org.workcraft.dom.visual.connections;

import java.awt.Graphics2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.util.Geometry;
import org.workcraft.util.XmlUtil;
import org.workcraft.util.Geometry.CurveSplitResult;

class Bezier implements ConnectionGraphic {
	private CubicCurve2D curve = new CubicCurve2D.Double();
	private CubicCurve2D visibleCurve = new CubicCurve2D.Double();

	private BezierAnchorPoint cp1, cp2;

	private double ax, bx, cx, ay, by, cy;
	private Rectangle2D boundingBox = null;

	private ConnectionInfo parentConnection;

	public Bezier(ConnectionInfo parentConnection) {
		this.parentConnection = parentConnection;

		cp1 = new BezierAnchorPoint(parentConnection, true);
		cp2 = new BezierAnchorPoint(parentConnection, false);

		cp1.setPosition(parentConnection.getPoint1());
		cp2.setPosition(parentConnection.getPoint2());
	}

	private void updateCoefficients() {
		Point2D p1 = parentConnection.getPoint1();
		Point2D p2 = parentConnection.getPoint2();

		cx = 3.0f * (cp1.getX() - p1.getX());
		bx = 3.0f * (cp2.getX() - cp1.getX()) - cx;
		ax = p2.getX() - p1.getX() - cx - bx;

		cy = 3.0f * (cp1.getY() - p1.getY());
		by = 3.0f * (cp2.getY() - cp1.getY()) - cy;
		ay = p2.getY() - p1.getY() - cy - by;

//		System.out.printf("ax=%8.4f bx=%8.4f cx=%8.4f\n", ax, bx, cx);

	//	coeff_valid = true;
	}

	public void draw(Graphics2D g) {
		g.draw(visibleCurve);
	}

	public VisualConnectionAnchorPoint[] getAnchorPointComponents() {
		VisualConnectionAnchorPoint[] ret = {cp1, cp2};
		return ret;
	}

	public Rectangle2D getBoundingBox() {
		return boundingBox;
	}


	public Point2D getNearestPointOnConnection(Point2D pt) {
		// TODO Auto-generated method stub
		return null;
	}


	// finds cubic root in real numbers
	public double curt(double a) {
		if (a==0) return 0;
		double s = Math.signum(a);
		return s*Math.exp(Math.log(s*a)/3);
	}

	public LinkedList<Double> solveCubic(double a, double b, double c, double d) {

		LinkedList<Double> ret= new LinkedList<Double>();
		double epsilon = 0.00000000001;

		if (a==0) {
			if (b==0) {
				if (c!=0)
					ret.add(-d/c);
				return ret;
			}

			// solveQuadratic(b, c, d)
			double D = c*c-4*b*d;
			if (Math.abs(D)<epsilon) {
				ret.add(-c/(2*b));
				return ret;
			}

			if (D>0) {
				ret.add((-c+Math.sqrt(D))/(2*b));
				ret.add((-c-Math.sqrt(D))/(2*b));
			}
			return ret;

		}

		double f = ((3*c/a)-(b*b/(a*a)))/3;
		double g = ((2*b*b*b/(a*a*a)) - (9*b*c/(a*a)) + (27*d/a))/27;
		double h = (g*g/4)+(f*f*f/27);

		if (h<=0) {
			if (g==0&&f==0) {
//				System.out.printf("all in same position\n");
				// all roots in the same position
				double x = curt(d/a);
				ret.add(x);
				return ret;
			}
//			System.out.printf("3 roots\n");
			// all three real roots
			double i= Math.sqrt(g*g/4-h);
			double j= curt(i);
			double K= Math.acos(-(g/(2*i)));
			double L= j*-1;
			double M= Math.cos(K/3);
			double N= Math.sqrt(3)*Math.sin(K/3);
			double P= -(b/(3*a));
			double x1 = 2*j* Math.cos(K/3) - (b/(3*a));
			double x2 = L*(M+N)+P;
			double x3 = L*(M-N)+P;
			ret.add(x1);
			ret.add(x2);
			ret.add(x3);
			return ret;
		} else {
//			System.out.printf("only one real root\n");
			// only one root is real
			double R = -(g/2)+Math.sqrt(h);
			double S = curt(R);
			double T = -(g/2)-Math.sqrt(h);
			double U = curt(T);
			double x = (S+U)-(b/(3*a));
			ret.add(x);
			return ret;
		}
	}

	public boolean touchesRectangle(Rectangle2D rect) {

		if (!curve.intersects(rect)) return false;

		LinkedList<Double> ROOTS;

		Point2D point1 = parentConnection.getPoint1();

//		boolean found = false;
//		System.out.printf("X=%6.5f Y=%6.5f\n", rect.getMinX(), rect.getMinY());
		ROOTS = solveCubic(ax, bx, cx, point1.getX()-rect.getMinX());
		for (Double r : ROOTS) {
			Point2D P = getPointOnConnection(r);
			double Y = P.getY();
//			double X = P.getX();
//			System.out.printf("      R=%14.5f X(R)=%14.5f Y(R)=%14.5f\n", r, X, Y);
			if (Y>=rect.getMinY()&&Y<=rect.getMaxY()) return true;
		}
//		if (found) return true;


		ROOTS = solveCubic(ax, bx, cx, point1.getX()-rect.getMaxX());
		for (Double r : ROOTS) {
			Point2D P = getPointOnConnection(r);
			double Y = P.getY();
//			double X = P.getX();
//			System.out.printf("      R=%14.5f X(R)=%14.5f Y(R)=%14.5f\n", r, X, Y);
			if (Y>=rect.getMinY()&&Y<=rect.getMaxY()) return true;
		}

		ROOTS = solveCubic(ay, by, cy, point1.getY()-rect.getMinY());
		for (Double r : ROOTS) {
			Point2D P = getPointOnConnection(r);
//			double Y = P.getY();
			double X = P.getX();
//			System.out.printf("      R=%14.5f X(R)=%14.5f Y(R)=%14.5f\n", r, X, Y);
			if (X>=rect.getMinX()&&X<=rect.getMaxX()) return true;
		}

		ROOTS = solveCubic(ay, by, cy, point1.getY()-rect.getMaxY());
		for (Double r : ROOTS) {
			Point2D P = getPointOnConnection(r);
//			double Y = P.getY();
			double X = P.getX();
//			System.out.printf("      R=%14.5f X(R)=%14.5f Y(R)=%14.5f\n", r, X, Y);
			if (X>=rect.getMinX()&&X<=rect.getMaxX()) return true;
		}

		return false;
	}

	public Point2D getPointOnConnection(double t) {

		double tSquared = t * t;
		double tCubed = tSquared * t;

		double x = (ax * tCubed) + (bx * tSquared) + (cx * t) + parentConnection.getPoint1().getX();
		double y = (ay * tCubed) + (by * tSquared) + (cy * t) + parentConnection.getPoint1().getY();

		return new Point2D.Double(x, y);
	}

	public void readFromXML(Element element, ConnectionInfo parent) {
		Element anchors;
		anchors = XmlUtil.getChildElement("anchorPoints", element);
		if (anchors==null) return;
		List<Element> xap = XmlUtil.getChildElements(VisualConnectionAnchorPoint.class.getSimpleName(), anchors);
		if (xap==null) return;

		Element eap = xap.get(0);
		cp1.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
		cp1.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));
		cp1.addPropertyChangeListener(new PropertyChangeListener() {
			public void onPropertyChanged(String propertyName, Object sender) {
				parentConnection.update();
			}
		});
		eap=xap.get(1);
		cp2.setX(XmlUtil.readDoubleAttr(eap, "X", 0));
		cp2.setY(XmlUtil.readDoubleAttr(eap, "Y", 0));
		cp2.addPropertyChangeListener(new PropertyChangeListener() {
			public void onPropertyChanged(String propertyName, Object sender) {
				parentConnection.update();
			}
		});
		parent.update();

	}

	public void writeToXML(Element element) {
		Element anchors = XmlUtil.createChildElement("anchorPoints", element);
		Element xap = XmlUtil.createChildElement(VisualConnectionAnchorPoint.class.getSimpleName(), anchors);
		XmlUtil.writeDoubleAttr(xap, "X", cp1.getX());
		XmlUtil.writeDoubleAttr(xap, "Y", cp1.getY());
		xap = XmlUtil.createChildElement(VisualConnectionAnchorPoint.class.getSimpleName(), anchors);
		XmlUtil.writeDoubleAttr(xap, "X", cp2.getX());
		XmlUtil.writeDoubleAttr(xap, "Y", cp2.getY());
	}

	public void removeAllAnchorPoints() {
		cp1.setPosition(parentConnection.getPoint1());
		cp2.setPosition(parentConnection.getPoint2());
	}

	public void removeAnchorPoint(VisualConnectionAnchorPoint anchor) {
		if (anchor==cp1) cp1.setPosition(parentConnection.getPoint1());
		if (anchor==cp2) cp2.setPosition(parentConnection.getPoint2());
	}

	private CubicCurve2D getPartialCurve(double tStart, double tEnd)
	{
		CubicCurve2D result = new CubicCurve2D.Double();

		CubicCurve2D fullCurve = new CubicCurve2D.Double();
		fullCurve.setCurve(parentConnection.getPoint1(), cp1.getPosition(), cp2.getPosition(), parentConnection.getPoint2());

		CurveSplitResult firstSplit = Geometry.splitCubicCurve(fullCurve, tStart);
		CurveSplitResult secondSplit = Geometry.splitCubicCurve(fullCurve, tEnd);

		result.setCurve(firstSplit.splitPoint, firstSplit.control2, secondSplit.control1, secondSplit.splitPoint);

		return result;
	}

	public void updateVisibleRange(double tStart, double tEnd) {
		visibleCurve = getPartialCurve(tStart, tEnd);
	}


	public void update() {
		updateCoefficients();

		boundingBox = curve.getBounds2D();
		boundingBox.add(boundingBox.getMinX()-VisualConnection.hitThreshold, boundingBox.getMinY()-VisualConnection.hitThreshold);
		boundingBox.add(boundingBox.getMinX()-VisualConnection.hitThreshold, boundingBox.getMaxY()+VisualConnection.hitThreshold);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.hitThreshold, boundingBox.getMinY()-VisualConnection.hitThreshold);
		boundingBox.add(boundingBox.getMaxX()+VisualConnection.hitThreshold, boundingBox.getMaxY()+VisualConnection.hitThreshold);
	}

	public void cleanup() {
		removeAllAnchorPoints();
	}

	public void click(Point2D point) {

	}

	public Collection<Node> getControls() {
		ArrayList<Node> result = new ArrayList<Node>();
		result.add(cp1);
		result.add(cp2);
		return result;
	}
}
