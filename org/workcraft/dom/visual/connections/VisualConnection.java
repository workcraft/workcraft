package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DependentNode;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformDispatcher;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformObserver;

public class VisualConnection extends VisualNode implements
		PropertyChangeListener, Node, Drawable, Connection,
		TransformObserver, DependentNode, VisualConnectionInfo {

	public enum ConnectionType
	{
		POLYLINE,
		BEZIER
	};

	private TransformDispatcher transformDispatcher = null;

	private MathConnection refConnection;
	private VisualComponent first;
	private VisualComponent second;

	private ConnectionType connectionType = ConnectionType.POLYLINE;

	private Point2D firstCenter = new Point2D.Double();
	private Point2D secondCenter = new Point2D.Double();
	private Point2D arrowHeadPosition = new Point2D.Double();
	private double arrowOrientation = 0;

	private ConnectionGraphic graphic = new Polyline(this);

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	public static double HIT_THRESHOLD = 0.2;
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

	public void setDependencies(VisualComponent first, VisualComponent second, MathConnection refConnection) {
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

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType t) {
		if (connectionType!=t) {
			if (t==ConnectionType.POLYLINE) {
				graphic = new Polyline(this);
			}
			if (t==ConnectionType.BEZIER) {
				graphic = new Bezier();
			}
			connectionType = t;
			graphic.setParent(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getColor()
	 */
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getLineWidth()
	 */
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

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getArrowWidth()
	 */
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

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getArrowLength()
	 */
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
		g.setStroke(new BasicStroke((float)lineWidth));
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

		if (graphic.getNearestPointOnConnection(pointInParentSpace).distance(pointInParentSpace) < HIT_THRESHOLD)
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

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getSecond()
	 */
	public VisualComponent getSecond() {
		return second;
	}

	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
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

	public ConnectionGraphic getGraphic() {
		return graphic;
	}

	@Override
	public Collection<Node> getChildren() {
		return Arrays.asList(new Node[] { graphic });
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getFirstCenter()
	 */
	public Point2D getFirstCenter() {
		return firstCenter;
	}

	public Point2D getSecondCenter() {
		return secondCenter;
	}
}