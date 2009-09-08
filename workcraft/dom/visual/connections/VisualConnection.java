package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformDispatcher;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModelEventDispatcher;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.EventListener1;
import org.workcraft.framework.observation.TransformChangedEvent;
import org.workcraft.framework.observation.TransformObserver;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.XmlUtil;

public class VisualConnection extends VisualNode
implements PropertyChangeListener, Node, Drawable, ConnectionInfo, Connection, TransformObserver {
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

	private TransformDispatcher transformDispatcher = null;

	protected MathConnection refConnection;
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
	}

	public VisualConnection() {

	}

	public void setVisualConnection(VisualComponent first, VisualComponent second, MathConnection refConnection) {
		this.first = first;
		this.second = second;
		this.refConnection = refConnection;

		if (transformDispatcher!=null) {
			transformDispatcher.subscribe(this, first);
			transformDispatcher.subscribe(this, second);
		}

		initialise();
	}

	public VisualConnection(MathConnection refConnection, VisualComponent first, VisualComponent second) {
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

	private double getBorderPoint (Touchable collisionNode, double tStart, double tEnd)
	{
		Point2D point = new Point2D.Double();

		while(Math.abs(tEnd-tStart) > 1e-6)
		{
			double t = (tStart + tEnd)*0.5;
			point = getPointOnConnection(t);

			if (collisionNode.hitTest(point))
				tStart = t;
			else
				tEnd = t;
		}

		return tStart;
	}

	public void update() {
		if (getParent() == null || first == null || second == null)
			return;

		Touchable firstTouchable = TransformHelper.transform(first, TransformHelper.getTransform(first, this));
		Touchable secondTouchable = TransformHelper.transform(second, TransformHelper.getTransform(second, this));

		Rectangle2D firstBB = firstTouchable.getBoundingBox();
		Rectangle2D secondBB = secondTouchable.getBoundingBox();

		firstCenter.setLocation(firstBB.getCenterX(), firstBB.getCenterY());
		secondCenter.setLocation(secondBB.getCenterX(), secondBB.getCenterY());

		Point2D pt = new Point2D.Double();

		graphic.update();

		// find connection curve starting point
		double tStart = getBorderPoint(firstTouchable, 0, 1);
		double tEnd = getBorderPoint(secondTouchable, 1, 0);

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
	public void setParent(Node parent) {
		super.setParent(parent);

		update();

	};

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

	public MathConnection getReferencedConnection() {
		return refConnection;
	}

//	public double distanceToConnection (Point2D pointInParentSpace) {
//		return impl.getDistanceToConnection(pointInParentSpace);
//	}

	public boolean hitTest(Point2D pointInParentSpace) {
/*		Rectangle2D rect = new Rectangle2D.Double(
				pointInParentSpace.getX()-hitThreshold,
				pointInParentSpace.getY()-hitThreshold,
				2*hitThreshold,
				2*hitThreshold
				);
		return TouchableHelper.touchesRectangle(this, rect); */

		if (graphic.getNearestPointOnConnection(pointInParentSpace).distance(pointInParentSpace) < hitThreshold)
			return true;
		else
			return false;
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

	public Collection<Node> getChildren() {
		return this.graphic.getControls();
	}

	@Override
	public Node getConnection() {
		return this;
	}

	@Override
	public Point2D getPoint1() {
		return firstCenter;
	}

	@Override
	public Point2D getPoint2() {
		return secondCenter;
	}

	@Override
	public void notify(TransformChangedEvent e) {
		update();
	}

	@Override
	public void subscribe(TransformDispatcher dispatcher) {
		if (first != null && second != null) {
			dispatcher.subscribe(this, first);
			dispatcher.subscribe(this, second);
		} else
			this.transformDispatcher = dispatcher;
	}
}