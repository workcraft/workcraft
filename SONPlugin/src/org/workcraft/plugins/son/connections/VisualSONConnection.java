package org.workcraft.plugins.son.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.ComponentsTransformObserver;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchyImpl;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualSONConnection extends VisualConnection
{

	public enum SONConnectionType {
		POLYLINE("Polyline"),
		BEZIER("Bezier"),
		SYNCLINE("Synchronous Communication"),
		ASYNLINE("Asynchronous Communication"),
		BHVLINE("Behavioural Abstraction");

		private final String name;

		private SONConnectionType(String name) {
			this.name = name;
		}

		static public Map<String, SONConnectionType> getChoice() {
			LinkedHashMap<String, SONConnectionType> choice = new LinkedHashMap<String, SONConnectionType>();
			for (SONConnectionType item : SONConnectionType.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
	};

	private ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();

	private SONConnection refConnection;
	private VisualComponent first;
	private VisualComponent second;

	private SONConnectionType connectionType = SONConnectionType.POLYLINE;

	private ConnectionGraphic graphic = null;

	private static double defaultLineWidth = 0.02;
	private static double defaultArrowWidth = 0.15;
	private static double defaultArrowLength = 0.4;
	public static double HIT_THRESHOLD = 0.2;
	private double lineWidth = defaultLineWidth;
	private double arrowWidth = defaultArrowWidth;
	private double arrowLength = defaultArrowLength;

	private HashSet<Node> children = new HashSet<Node>();
	private ComponentsTransformObserver componentsTransformObserver = null;

	protected void initialise() {
		addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, Double>(
				this, "Line width", Double.class) {
			protected void setter(VisualSONConnection object, Double value) {
				object.setLineWidth(value);
			}
			protected Double getter(VisualSONConnection object) {
				return object.getLineWidth();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, Double>(
				this, "Arrow width", Double.class) {
			protected void setter(VisualSONConnection object, Double value) {
				object.setArrowWidth(value);
			}
			protected Double getter(VisualSONConnection object) {
				return object.getArrowWidth();
			}
		});

		LinkedHashMap<String, Double> arrowLengths = new LinkedHashMap<String, Double>();
		arrowLengths.put("short", 0.2);
		arrowLengths.put("medium", 0.4);
		arrowLengths.put("long", 0.8);
		addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, Double>(
				this, "Arrow length", Double.class, arrowLengths) {
			protected void setter(VisualSONConnection object, Double value) {
				object.setArrowLength(value);
			}
			protected Double getter(VisualSONConnection object) {
				return object.getArrowLength();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, SONConnectionType>(
				this, "Connection type", SONConnectionType.class, SONConnectionType.getChoice()) {
			protected void setter(VisualSONConnection object, SONConnectionType value) {
				object.setSONConnectionType(value);
			}
			protected SONConnectionType getter(VisualSONConnection object) {
				return object.getSONConnectionType();
			}
		});


		addPropertyDeclaration(new PropertyDeclaration<VisualSONConnection, ScaleMode>(
				this, "Scale mode", ScaleMode.class, ScaleMode.getChoice()) {
			protected void setter(VisualSONConnection object, ScaleMode value) {
				object.setScaleMode(value);
			}
			protected ScaleMode getter(VisualSONConnection object) {
				return object.getScaleMode();
			}
		});

		componentsTransformObserver = new ComponentsTransformObserver(this);
		children.add(componentsTransformObserver);
		children.add(graphic);
	}

	public VisualSONConnection() {
	}

	public VisualSONConnection(SONConnection refConnection) {
		this.refConnection = refConnection;
	}

	public VisualSONConnection(SONConnection refConnection, VisualComponent first, VisualComponent second) {

		this.refConnection = refConnection;
		this.first = first;
		this.second = second;
		this.graphic = new Polyline(this);

		initialise();
	}

	@NoAutoSerialisation
	public SONConnectionType getSONConnectionType() {
		return connectionType;
	}

	public void setVisualConnectionDependencies(VisualComponent first, VisualComponent second, ConnectionGraphic graphic, SONConnection refConnection) {
		if(first == null)
			throw new NullPointerException("first");
		if(second == null)
			throw new NullPointerException("second");
		if(graphic == null)
			throw new NullPointerException("graphic");

		this.first = first;
		this.second = second;
		this.refConnection = refConnection;
		this.graphic = graphic;

		if (graphic instanceof Polyline)
			this.connectionType = SONConnectionType.POLYLINE;
		if (graphic instanceof SyncLine)
			this.connectionType = SONConnectionType.SYNCLINE;
		if (graphic instanceof AsynLine)
			this.connectionType = SONConnectionType.ASYNLINE;
		if (graphic instanceof BhvLine)
			this.connectionType = SONConnectionType.BHVLINE;
		else if (graphic instanceof Bezier)
			this.connectionType = SONConnectionType.BEZIER;

		initialise();
	}

	@NoAutoSerialisation
	public void setSONConnectionType(SONConnectionType t) {
		if (connectionType!=t) {
			observableHierarchyImpl.sendNotification(new NodesDeletingEvent(this, graphic));
			children.remove(graphic);
			observableHierarchyImpl.sendNotification(new NodesDeletedEvent(this, graphic));

			if (t==SONConnectionType.POLYLINE) {
				Polyline p  = new Polyline(this);
				this.graphic = p;

				VisualComponent v = this.getFirst();
				if (v == this.getSecond()) {
					ControlPoint cp1 = new ControlPoint();
					cp1.setPosition(new Point2D.Double(v.getX()-1.0, v.getY()+1.5));
					ControlPoint cp2 = new ControlPoint();
					cp2.setPosition(new Point2D.Double(v.getX()+1.0, v.getY()+1.5));
					p.add(cp1);
					p.add(cp2);
				}
			}

			if (t==SONConnectionType.BEZIER) {
				Bezier b = new Bezier(this);
				b.setDefaultControlPoints();
				graphic = b;

				VisualComponent v = this.getFirst();
				if (v == this.getSecond()) {
					BezierControlPoint[] cp = b.getControlPoints();
					cp[0].setPosition(new Point2D.Double(v.getX()-2.0, v.getY()+2.0));
					cp[1].setPosition(new Point2D.Double(v.getX()+2.0, v.getY()+2.0));
				}
			}

			if (t==SONConnectionType.SYNCLINE){
				SyncLine s = new SyncLine(this);
				graphic = s;
			}

			if (t==SONConnectionType.ASYNLINE){
				AsynLine s = new AsynLine(this);
				graphic = s;

				setArrowWidth(0.3);
				setArrowLength(0.8);
			}

			if (t==SONConnectionType.BHVLINE){
				BhvLine s = new BhvLine(this);
				graphic = s;

				setArrowWidth(0.20);
				setArrowLength(0.4);
			}

			children.add(graphic);
			observableHierarchyImpl.sendNotification(new NodesAddedEvent(this, graphic));

			graphic.invalidate();
			connectionType = t;
			observableStateImpl.sendNotification(new PropertyChangedEvent(this, "SONConnectionType"));
		}
	}

	public void setMathConnectionType(String type){
		SONConnection con = (SONConnection)this.getReferencedConnection();
		con.setType(type);
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

		invalidate();
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

		invalidate();
	}

	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getArrowLength()
	 */
	public double getArrowLength()
	{
		if (!hasArrow()) return 0.0;
		return arrowLength;
	}

	public void setArrowLength(double arrowLength) {
		if (arrowLength > 1)
			arrowLength = 1;
		if (arrowLength < 0.1)
			arrowLength = 0.1;
		this.arrowLength = arrowLength;

		invalidate();
	}

	private void invalidate() {
		if (graphic != null)
			graphic.invalidate();
	}
	@Override
	public Point2D getPointOnConnection(double t) {
		return graphic.getPointOnCurve(t);
	}
	@Override
	public Point2D getNearestPointOnConnection(Point2D pt) {
		return graphic.getNearestPointOnCurve(pt);
	}

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);

		invalidate();
	};

	@Override
	public void draw(DrawRequest r) {

	}
	@Override
	public SONConnection getReferencedConnection() {
		return refConnection;
	}
	@Override
	public boolean hitTest(Point2D pointInParentSpace) {
		return graphic.hitTest(pointInParentSpace);
	}

	@Override
	public Rectangle2D getBoundingBox() {
		return graphic.getBoundingBox();
	}
	@Override
	public VisualComponent getFirst() {
		return first;
	}
	@Override
	public VisualComponent getSecond() {
		return second;
	}

	@Override
	public Set<MathNode> getMathReferences() {
		Set<MathNode> ret = new HashSet<MathNode>();
		ret.add(getReferencedConnection());
		return ret;
	}

	public ConnectionGraphic getGraphic() {
		return graphic;
	}

	@Override
	public Collection<Node> getChildren() {
		return children;
	}

	@Override
	public Color getDrawColor() {
		return this.getReferencedConnection().getColor();
	}

	public void setColor(Color color) {
		this.getReferencedConnection().setColor(color);
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		observableHierarchyImpl.addObserver(obs);
	}
	@Override
	public void removeObserver(HierarchyObserver obs) {
		observableHierarchyImpl.removeObserver(obs);
	}

	@Override
	public boolean hasArrow() {
		if(this.getSONConnectionType() == SONConnectionType.SYNCLINE )
			return false;
		return true;
	}

	@Override
	public Point2D getFirstCenter() {
		return componentsTransformObserver.getFirstCenter();
	}

	@Override
	public Touchable getFirstShape() {
		return componentsTransformObserver.getFirstShape();
	}

	@Override
	public Point2D getSecondCenter() {
		return componentsTransformObserver.getSecondCenter();
	}

	@Override
	public Touchable getSecondShape() {
		return componentsTransformObserver.getSecondShape();
	}

	@Override
	public Point2D getCenter()
	{
		return graphic.getCenter();
	}

	@Override
	public Stroke getStroke()
	{
		return new BasicStroke((float)getLineWidth());
	}


}
