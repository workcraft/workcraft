package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.TouchableHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModelEventDispatcher;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.EventListener1;
import org.workcraft.framework.exceptions.NotAnAncestorException;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.XmlUtil;

public class VisualConnection extends VisualNode implements PropertyChangeListener, HierarchyNode, Drawable {
	public enum ConnectionType
	{
		POLYLINE,
		BEZIER
	};

	private EventListener1<Set<VisualNode>> selectionListener = new EventListener1<Set<VisualNode>> () {
		//private Set<VisualNode> editorControls;

		public void eventFired(Set<VisualNode> selection) {
/*			boolean needsEditor;

			if(selection.contains(VisualConnection.this))
				needsEditor = true;
			else
				if(editorVisible)
				{
					for(VisualNode control : VisualConnection.this.graphic.getControls())
						if(selection.contains(control))
							needsEditor = true;
					needsEditor = false;
				}
				else
					needsEditor =  false;

			if(!editorVisible && needsEditor)
			{
				editorControls = VisualConnection.this.graphic.getControls();
			}
		*/
		}
	};

	protected Connection refConnection;
	protected VisualComponent first;
	protected VisualComponent second;

	private ConnectionType connectionType = ConnectionType.POLYLINE;

	private Point2D firstCenter = new Point2D.Double();
	private Point2D secondCenter = new Point2D.Double();
	private Point2D arrowHeadPosition = new Point2D.Double();
	private double arrowOrientation = 0;

	private ConnectionGraphic graphic = new Polyline(this);

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	public static double hitThreshold = 0.2;
	private static Color defaultColor = Color.BLACK;

	private Color color = defaultColor;
	private double lineWidth = defaultLineWidth;
	private double arrowWidth = defaultArrowWidth;
	private double arrowLength = defaultArrowLength;

	protected void initialise() {
		addListeners();

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
	}

	private void addListeners() {
		first.addPropertyChangeListener(this);
		second.addPropertyChangeListener(this);

		update();
	}

	public VisualConnection() {

	}

	public void setVisualConnection(VisualComponent first, VisualComponent second, Connection refConnection) {
		this.first = first;
		this.second = second;
		this.refConnection = refConnection;

		initialise();
		addListeners();
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
		graphic.writeToXML(element);
	}

	protected void readXMLConnectionProperties(Element element) {
		String strConnectionType = XmlUtil.readStringAttr(element, "type");
		if (!strConnectionType.equals("")) {
			setConnectionType(ConnectionType.valueOf(strConnectionType));
		}
		setArrowLength(XmlUtil.readDoubleAttr(element, "arrowLength", defaultArrowLength));
		setArrowWidth(XmlUtil.readDoubleAttr(element, "arrowWidth", defaultArrowWidth));
		setLineWidth(XmlUtil.readDoubleAttr(element, "lineWidth", defaultLineWidth));

		graphic.readFromXML(element, VisualConnection.this);
	}

	public ConnectionType getConnectionType() {
		return connectionType;

	}

	public void setConnectionType(ConnectionType t) {
		if (connectionType!=t) {
			graphic.cleanup();
			if (t==ConnectionType.POLYLINE) {
				graphic = new Polyline(this);
			}
			if (t==ConnectionType.BEZIER) {
				graphic = new Bezier(this);
			}
			connectionType = t;
		}
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
		return graphic.getPointOnConnection(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return graphic.getNearestPointOnConnection(pt);
	}

	public void click (Point2D point) {
		graphic.click(point);
	}

	private double getBorderPoint (VisualComponent collisionComponent, AffineTransform toComponentParent, double tStart, double tEnd)
	{
		Point2D point = new Point2D.Double();

		while(Math.abs(tEnd-tStart) > 1e-6)
		{
			double t = (tStart + tEnd)*0.5;
			point = getPointOnConnection(t);

			toComponentParent.transform(point, point);
			if (collisionComponent.hitTest(point))
				tStart = t;
			else
				tEnd = t;
		}

		return tStart;
	}

	public void update() {
		if (getParent() == null)
			return;
		AffineTransform t1,t2;
		try {
			t1 = TransformHelper.getTransform(first, this);
			t2 = TransformHelper.getTransform(second, this);
		} catch (NotAnAncestorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		// get centres of the two components in this connection's parent space
		Rectangle2D firstBB = first.getBoundingBox();
		Rectangle2D secondBB = second.getBoundingBox();

		firstCenter.setLocation(firstBB.getCenterX(), firstBB.getCenterY());
		secondCenter.setLocation(secondBB.getCenterX(), secondBB.getCenterY());

		t1.transform(firstCenter, firstCenter);
		t2.transform(secondCenter, secondCenter);

		// create transforms from this connection's parent space to
		// components' parent spaces, for hit testing
		AffineTransform it1;
		AffineTransform it2;
		try {
			it1 = t1.createInverse();
			it2 = t2.createInverse();
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
			return;
		}

		Point2D pt = new Point2D.Double();

		graphic.update();

		// find connection curve starting point
		double tStart = getBorderPoint(first, it1, 0, 1);
		double tEnd = getBorderPoint(second, it2, 1, 0);

		Point2D pointEnd = getPointOnConnection(tEnd);

		arrowHeadPosition = pointEnd;

		double dt = tEnd;
		double t = 0.0;

		while(dt > 1e-6)
		{
			dt /= 2.0;
			t += dt;
			pt = getPointOnConnection(t);
			if (arrowHeadPosition.distanceSq(pt) < arrowLength*arrowLength)
				t-=dt;
		}

		arrowOrientation = Math.atan2(arrowHeadPosition.getY() - pt.getY() , arrowHeadPosition.getX() - pt.getX());

		graphic.updateVisibleRange(tStart, t);
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
		//Shape transformedArrowBounds = arrowTransform.createTransformedShape(arrowBounds);

		graphic.draw(g);

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

//	public double distanceToConnection (Point2D pointInParentSpace) {
//		return impl.getDistanceToConnection(pointInParentSpace);
//	}

	public boolean hitTest(Point2D pointInParentSpace) {
		Rectangle2D rect = new Rectangle2D.Double(
				pointInParentSpace.getX()-hitThreshold,
				pointInParentSpace.getY()-hitThreshold,
				2*hitThreshold,
				2*hitThreshold
				);
		return TouchableHelper.touchesRectangle(this, rect);
//		if (distanceToConnection(pointInParentSpace) < hitThreshold)
//			return 1;
//		else
//			return 0;
	}

	@Override
	public Rectangle2D getBoundingBox() {
		return graphic.getBoundingBox();
	}

	public void onPropertyChanged(String propertyName, Object sender) {
		if (propertyName.equals("X") || propertyName.equals("Y") || propertyName.equals("transform") || propertyName.equals("shape"));
		update();
	}

	public VisualComponent getFirst() {
		return first;
	}

	public VisualComponent getSecond() {
		return second;
	}

	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
	}

	public void subscribeEvents(VisualModelEventDispatcher eventDispatcher) {
		eventDispatcher.addSelectionChangedListener(selectionListener);

	}

	public void unsubscribeEvents(VisualModelEventDispatcher eventDispatcher) {
		eventDispatcher.removeSelectionChangedListener(selectionListener);
	}

	public Collection<HierarchyNode> getChildren() {
		return this.graphic.getControls();
	}
}