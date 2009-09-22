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
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.ObservableHierarchyImpl;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformObserver;

public class VisualConnection extends VisualNode implements
		PropertyChangeListener, Node, Drawable, Connection,
		TransformObserver, DependentNode, VisualConnectionInfo,
		ObservableHierarchy {

	public enum ConnectionType
	{
		POLYLINE,
		BEZIER
	};

	private TransformDispatcher transformDispatcher = null;
	private ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();

	private MathConnection refConnection;
	private VisualComponent first;
	private VisualComponent second;

	private ConnectionType connectionType = ConnectionType.POLYLINE;

	private Point2D firstCenter = new Point2D.Double();
	private Point2D secondCenter = new Point2D.Double();

	private Touchable firstShape;
	private Touchable secondShape;

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
			observableHierarchyImpl.sendNotification(new NodesDeletingEvent(this, graphic));
			observableHierarchyImpl.sendNotification(new NodesDeletedEvent(this, graphic));

			if (t==ConnectionType.POLYLINE) {
				graphic = new Polyline(this);
			}
			if (t==ConnectionType.BEZIER) {
				graphic = new Bezier(this);
			}

			observableHierarchyImpl.sendNotification(new NodesAddedEvent(this, graphic));

			graphic.update();
			connectionType = t;
			observableStateImpl.sendNotification(new PropertyChangedEvent(this, "connectionType"));
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
		return graphic.getPointOnCurve(t);
	}

	public Point2D getNearestPointOnConnection(Point2D pt) {
		return graphic.getNearestPointOnCurve(pt);
	}

	public void update() {
		if (getParent() == null || first == null || second == null)
			return;

		firstShape = TransformHelper.transform(first, TransformHelper.getTransform(first, this));
		secondShape = TransformHelper.transform(second, TransformHelper.getTransform(second, this));

		Rectangle2D firstBB = firstShape.getBoundingBox();
		Rectangle2D secondBB = secondShape.getBoundingBox();

		firstCenter.setLocation(firstBB.getCenterX(), firstBB.getCenterY());
		secondCenter.setLocation(secondBB.getCenterX(), secondBB.getCenterY());

		graphic.update();
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

	public boolean hitTest(Point2D pointInParentSpace) {
		return graphic.hitTest(pointInParentSpace);
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

	public Point2D getFirstCenter() {
		return firstCenter;
	}

	public Point2D getSecondCenter() {
		return secondCenter;
	}

	@Override
	public Touchable getFirstShape() {
		return firstShape;
	}

	@Override
	public Touchable getSecondShape() {
		return secondShape;
	}

	@Override
	public Color getDrawColor() {
		return Coloriser.colorise(color, getColorisation());
	}

	public void addObserver(HierarchyObserver obs) {
		observableHierarchyImpl.addObserver(obs);
	}

	public void removeObserver(HierarchyObserver obs) {
		observableHierarchyImpl.removeObserver(obs);
	}
}