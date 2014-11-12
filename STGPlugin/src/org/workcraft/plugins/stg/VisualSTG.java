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

package org.workcraft.plugins.stg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.propertydescriptors.SignalNamePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.util.Hierarchy;

@DisplayName("Signal Transition Graph")
@CustomTools(STGToolsProvider.class)
public class VisualSTG extends AbstractVisualModel {
	private STG stg;

	public VisualSTG(STG model) {
		this(model, null);
	}

	public VisualSTG(STG model, VisualGroup root) {
		super(model, root);
		this.stg = model;
		if (root == null) {
			try {
				createDefaultFlatStructure();
				// FIXME: Implicit places should not appear in the first place.
				fixVisibilityOfImplicitPlaces();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void fixVisibilityOfImplicitPlaces() {
		for (VisualPlace vp: getVisualPlaces()) {
			Place p = vp.getReferencedPlace();
			if (p instanceof STGPlace) {
				STGPlace pp = (STGPlace)p;
				if (pp.isImplicit()) {
					maybeMakeImplicit(vp, false);
				}
			}
		}
	}

	@Override
	public void validateConnection(Node first, Node second)	throws InvalidConnectionException {
		if (first == second) {
			throw new InvalidConnectionException ("Self-loops are not allowed.");
		}
		if (getConnection(first, second) != null) {
			throw new InvalidConnectionException ("This arc already exisits.");
		}
		if (((first instanceof VisualPlace) || (first instanceof VisualImplicitPlaceArc))
			&& ((second instanceof VisualPlace) || (second instanceof VisualImplicitPlaceArc))) {
			throw new InvalidConnectionException ("Arcs between places are not allowed.");
		}
	}

	@Override
	public VisualConnection connect(Node first, Node second) throws InvalidConnectionException {
		validateConnection(first, second);

		VisualConnection connection = null;
		if (first instanceof VisualTransition) {
			if (second instanceof VisualTransition) {
				connection = createImplicitPlaceConnection((VisualTransition) first, (VisualTransition) second);
			} else if (second instanceof VisualImplicitPlaceArc) {
				VisualImplicitPlaceArc con = (VisualImplicitPlaceArc)second;
				VisualPlace place = makeExplicit(con);
				connection = connect(first, place);
			} else if (second instanceof VisualPlace) {
				connection = createSimpleConnection((VisualComponent) first, (VisualComponent) second);
			}
		} else if (first instanceof VisualImplicitPlaceArc) {
			if (second instanceof VisualTransition) {
				VisualImplicitPlaceArc con = (VisualImplicitPlaceArc)first;
				VisualPlace place = makeExplicit(con);
				connection = connect(place, second);
			}
		} else {
			connection = createSimpleConnection((VisualComponent)first, (VisualComponent)second);
		}
		return connection;
	}

	private VisualImplicitPlaceArc createImplicitPlaceConnection(VisualTransition t1,
			VisualTransition t2) throws InvalidConnectionException {
		final ConnectionResult connectResult = stg.connect(t1.getReferencedTransition(), t2.getReferencedTransition());

		STGPlace implicitPlace = connectResult.getImplicitPlace();
		MathConnection con1 = connectResult.getCon1();
		MathConnection con2 = connectResult.getCon2();

		if (implicitPlace == null || con1 == null || con2 == null) {
			throw new NullPointerException();
		}
		VisualImplicitPlaceArc connection = new VisualImplicitPlaceArc(t1, t2, con1, con2, implicitPlace);
		Hierarchy.getNearestContainer(t1, t2).add(connection);
		return connection;
	}

	private VisualConnection createSimpleConnection(final VisualComponent firstComponent, final VisualComponent secondComponent)
			throws InvalidConnectionException {

		MathNode firstRef = firstComponent.getReferencedComponent();
		MathNode secondRef = secondComponent.getReferencedComponent();
		ConnectionResult result = stg.connect(firstRef, secondRef);

		MathConnection refConnection = result.getSimpleResult();
		if (refConnection == null) {
			throw new NullPointerException();
		}

		VisualConnection connection = new VisualConnection(refConnection, firstComponent, secondComponent);
		Hierarchy.getNearestContainer(firstComponent, secondComponent).add(connection);
		return connection;
	}

	public VisualPlace makeExplicit(VisualImplicitPlaceArc connection) {
		Container group = Hierarchy.getNearestAncestor(connection, Container.class);

		List<Point2D> locations = new LinkedList<Point2D>();
		int splitIndex = -1;
		if (connection.getGraphic() instanceof Polyline) {
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
			Polyline polyline = (Polyline)connection.getGraphic();
			for (ControlPoint cp:  polyline.getControlPoints()) {
				Point2D location = localToRootTransform.transform(cp.getPosition(), null);
				locations.add(location);
			}
			splitIndex = polyline.getNearestSegment(connection.getSplitPoint(), null);
		}

		STGPlace implicitPlace = connection.getImplicitPlace();
		stg.makeExplicit(implicitPlace);
		VisualPlace place = new VisualPlace(implicitPlace);
		place.setPosition(connection.getSplitPoint());

		VisualConnection con1 = new VisualConnection(connection.getRefCon1(), connection.getFirst(), place);
		VisualConnection con2 = new VisualConnection(connection.getRefCon2(), place, connection.getSecond());

		group.add(place);
		group.add(con1);
		group.add(con2);

		if (!locations.isEmpty()) {
			ConnectionHelper.addControlPoints(con1, locations.subList(0, splitIndex));
			ConnectionHelper.addControlPoints(con2, locations.subList(splitIndex, locations.size()));
		}

		remove(connection);
		return place;
	}

	public void maybeMakeImplicit(VisualPlace place, boolean preserveConnectionShape) {
		Collection<Node> preset = getPreset(place);
		Collection<Node> postset = getPostset(place);
		if ((preset.size() == 1) && (postset.size() == 1)) {
			final STGPlace stgPlace = (STGPlace)place.getReferencedPlace();
			stgPlace.setImplicit(true);
			VisualComponent first = (VisualComponent)preset.iterator().next();
			VisualComponent second = (VisualComponent)postset.iterator().next();

			MathConnection refCon1 = null, refCon2 = null;
			Collection<Connection> connections = new ArrayList<Connection> (getConnections(place));
			for (Connection con: connections) {
				if (con.getFirst() == place) {
					refCon2 = ((VisualConnection)con).getReferencedConnection();
				} else if (con.getSecond() == place) {
					refCon1 = ((VisualConnection)con).getReferencedConnection();
				}
			}
			VisualImplicitPlaceArc con = new VisualImplicitPlaceArc(first, second, refCon1, refCon2, (STGPlace)place.getReferencedPlace());
			if (preserveConnectionShape) {
				con.addPolylinePoint(place.getPosition(), true);
			}
			Container parent = Hierarchy.getNearestAncestor(Hierarchy.getCommonParent(first, second), Container.class);
			parent.add(con);
			// Remove explicit place, all its connections will get removed automatically by the hanging connection remover
			remove(place);
		}
	}

	public VisualPlace createPlace(String name, Container visualContainer) {

		if (visualContainer==null) visualContainer = getRoot();
		STGPlace p = stg.createPlace(name, getMathContainer(this, visualContainer));
		VisualPlace place = new VisualPlace(p);
		visualContainer.add(place);
		return place;
	}

	public VisualDummyTransition createDummyTransition(String name, Container container) {
		if (container==null) container = getRoot();

		DummyTransition transition = stg.createDummyTransition(name, getMathContainer(this, container));
		VisualDummyTransition visualTransition = new VisualDummyTransition(transition);

		container.add(visualTransition);
		return visualTransition;
	}


	public VisualSignalTransition createSignalTransition(String signalName, SignalTransition.Type type, Direction direction, Container container) {
		if (container==null) container = getRoot();

		String name = null;
		if ((signalName != null) && (direction != null)) {
			name = signalName + direction.toString();
		}
		SignalTransition transition = stg.createSignalTransition(name, getMathContainer(this, container));
		transition.setSignalType(type);
		VisualSignalTransition visualTransition = new VisualSignalTransition(transition);

		container.add(visualTransition);
		return visualTransition;
	}

	public Collection<VisualPlace> getVisualPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class);
	}

	public Collection<VisualImplicitPlaceArc> getVisualImplicitPlaceArcs() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualImplicitPlaceArc.class);
	}

	public Collection<VisualTransition> getVisualTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualTransition.class);
	}

	public Collection<VisualSignalTransition> getVisualSignalTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualSignalTransition.class);
	}

	public Collection<VisualDummyTransition> getVisualDummyTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), VisualDummyTransition.class);
	}

	public VisualTransition getVisualTransition(Transition transition) {
		for (VisualTransition vt: getVisualTransitions()) {
			if (vt.getReferencedTransition() == transition) {
				return vt;
			}
		}
		return null;
	}

	@Override
	public ModelProperties getProperties(Node node) {
		ModelProperties properties = super.getProperties(node);
		if (node == null) {
			for (Type type : Type.values()) {
				Container container = getMathContainer(this, getCurrentLevel());
				for (final String signalName : stg.getSignalNames(type, container)) {
					if (stg.getSignalTransitions(signalName, container).isEmpty()) continue;
					properties.add(new SignalNamePropertyDescriptor(stg, signalName, container));
					properties.add(new SignalTypePropertyDescriptor(stg, signalName, container));
				}
			}
		}
		return properties;
	}

}
