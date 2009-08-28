package org.workcraft.plugins.stg;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.HierarchyHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModelEventListener;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;

public class VisualSTG extends VisualPetriNet  {
	class Listener implements VisualModelEventListener {
		public void onComponentAdded(VisualComponent component) {
			if (component instanceof VisualSignalTransition)
				transitions.add((VisualSignalTransition)component);
		}

		public void onComponentPropertyChanged(String propertyName,
				VisualComponent component) {
			if (component instanceof VisualSignalTransition && propertyName.equals("Signal type")) {
				VisualSignalTransition t = (VisualSignalTransition)component;
				String signalName = t.getSignalName();
				if (signalName.isEmpty())
					return;

				for (VisualSignalTransition tt : transitions) {
					if (signalName.equals(tt.getSignalName())) {
						tt.setType(t.getType());
					}
				}
			}

			if (component instanceof VisualSignalTransition && propertyName.equals("Signal name")) {
				VisualSignalTransition t = (VisualSignalTransition)component;

				String signalName = t.getSignalName();

				if (signalName.isEmpty())
					return;

				for (VisualSignalTransition tt : transitions) {
					if (signalName.equals(tt.getSignalName())) {
						t.setType(tt.getType());
						break;
					}
				}

			}
		}

		public void onComponentRemoved(VisualComponent component) {
			if (component instanceof VisualSignalTransition)
				transitions.remove(component);
		}

		public void onConnectionAdded(VisualConnection connection) {
		}

		public void onConnectionPropertyChanged(String propertyName,
				VisualConnection connection) {
		}

		public void onConnectionRemoved(VisualConnection connection) {
		}

		public void onLayoutChanged() {
		}

		public void onSelectionChanged(Set<VisualNode> s) {
		}

		@Override
		public void onSelectionChanged(Collection<HierarchyNode> selection) {
			// TODO Auto-generated method stub

		}
	}

	private HashSet<VisualSignalTransition> transitions = new HashSet<VisualSignalTransition>();
	private HashSet<VisualPlace> lockedPlaces = new HashSet<VisualPlace>();


	@Override
	public void validateConnection(HierarchyNode first, HierarchyNode second)
	throws InvalidConnectionException {
		if (first instanceof VisualPlace) {
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places are not allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Connections between places and implicit places are not allowed");
		}

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualConnection)
				if (! (second  instanceof ImplicitPlaceArc))
					throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
		}

		if (first instanceof VisualConnection) {
			if (!(first instanceof ImplicitPlaceArc))
				throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Connections between arcs are not allowed");
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places and implicit places are not allowed");

			ImplicitPlaceArc con = (ImplicitPlaceArc) first;
			if (con.getFirst() == second || con.getSecond() == second)
				throw new InvalidConnectionException ("Arc already exists");
		}
	}

	@Override
	public VisualConnection connect(HierarchyNode first,
			HierarchyNode second) throws InvalidConnectionException {

		validateConnection(first, second);

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualSignalTransition) {
				STG mathModel = (STG)getMathModel();
				VisualSignalTransition t1 = (VisualSignalTransition) first;
				VisualSignalTransition t2 = (VisualSignalTransition) second;

				Place implicitPlace = mathModel.createPlace();
				Connection con1 = mathModel.connect(t1.getReferencedTransition(), implicitPlace);
				Connection con2 = mathModel.connect(implicitPlace, t2.getReferencedTransition());

				ImplicitPlaceArc connection = new ImplicitPlaceArc((VisualComponent)first, (VisualComponent)second, con1, con2, implicitPlace);

				Container group =
					HierarchyHelper.getNearestAncestor(
					HierarchyHelper.getCommonParent(first, second),
					Container.class);

				group.add(connection);
				addNode(connection);

				return connection;
			} else if (second instanceof ImplicitPlaceArc) {
				ImplicitPlaceArc con = (ImplicitPlaceArc)second;
				Container group = HierarchyHelper.getNearestAncestor(con, Container.class);

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());

				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());

				addNode(place);
				addNode(con1);
				addNode(con2);
				group.add(place);
				group.add(con1);
				group.add(con2);

				removeVisualConnectionOnly(con);

				return super.connect(first, place);
			}
		}


		if (first instanceof ImplicitPlaceArc)
			if (second instanceof VisualSignalTransition) {
				ImplicitPlaceArc con = (ImplicitPlaceArc)first;
				Container group = HierarchyHelper.getNearestAncestor(con, Container.class);

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());

				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());

				addNode(place);
				addNode(con1);
				addNode(con2);
				group.add(place);
				group.add(con1);
				group.add(con2);

				removeVisualConnectionOnly(con);

				return super.connect(place, second);
			}

		// do a default connection and check if it produced an implicit place

		VisualConnection ret = super.connect(first, second);

		VisualConnection implicit = null;

		if (ret.getFirst() instanceof VisualPlace)
			implicit = maybeMakeImplicit((VisualPlace)ret.getFirst());
		else if (ret.getSecond() instanceof VisualPlace)
			implicit = maybeMakeImplicit((VisualPlace)ret.getSecond());

		if (implicit != null)
			return implicit;
		else
			return ret;
	}

	private void removeVisualConnectionOnly(VisualConnection connection) {
		connection.getFirst().removeConnection(connection);
		connection.getSecond().removeConnection(connection);

		((Container)connection.getParent()).remove(connection);
		selection().remove(connection);
		connection.removePropertyChangeListener(getPropertyChangeListener());
	}

	private void removeVisualComponentOnly(VisualComponent component) {
		((Container)component.getParent()).remove(component);
		selection().remove(component);
		component.removePropertyChangeListener(getPropertyChangeListener());
	}

	private VisualConnection maybeMakeImplicit (VisualPlace place) {
		if (place.getPreset().size() != 1 || place.getPostset().size() != 1)
			return null; // not an implicit place

		Connection refCon1 = null, refCon2 = null;

		VisualComponent first = place.getPreset().iterator().next();
		VisualComponent second = place.getPostset().iterator().next();


		for (VisualConnection con:	place.getConnections()) {
			if (con.getFirst() == place)
				refCon2 = con.getReferencedConnection();
			else if (con.getSecond() == place)
				refCon1 = con.getReferencedConnection();

			removeVisualConnectionOnly(con);
		}

		removeVisualComponentOnly(place);

		ImplicitPlaceArc con = new ImplicitPlaceArc(first, second, refCon1, refCon2, place.getReferencedPlace());

		HierarchyHelper.getNearestAncestor(
				HierarchyHelper.getCommonParent(first, second), Container.class)
					.add(con);
		addNode(con);

		return con;
	}


	@Override
	protected void removeConnection(VisualConnection connection) {
		if (connection instanceof ImplicitPlaceArc) {

			//connection.removeAllAnchorPoints();
			connection.getFirst().removeConnection(connection);
			connection.getSecond().removeConnection(connection);

			getMathModel().removeConnection(((ImplicitPlaceArc) connection).getRefCon1());
			getMathModel().removeConnection(((ImplicitPlaceArc) connection).getRefCon2());
			getMathModel().removeComponent(((ImplicitPlaceArc) connection).getImplicitPlace());

			((Container)connection.getParent()).remove(connection);
			selection().remove(connection);

			connection.removePropertyChangeListener(getPropertyChangeListener());

			fireConnectionRemoved(connection);

		} else {
			super.removeConnection(connection);
		}
	}

	private void refreshImplicitPlaces() {
		for (VisualComponent c : getRoot().getComponents()) {
			if (c instanceof VisualPlace)
				if (!lockedPlaces.contains(c))
				maybeMakeImplicit((VisualPlace)c);
		}

	}

	@Override
	public void removeComponent(VisualComponent component) {
		if (component instanceof VisualPlace)
			lockedPlaces.add((VisualPlace)component);
		super.removeComponent(component);
		lockedPlaces.clear();
	}

	public VisualSTG(STG model) throws VisualModelInstantiationException {
		super(model);
		refreshImplicitPlaces();

		addListener(new Listener());
	}


	private void lockPlaces(Collection<HierarchyNode> nodes) {
		for (HierarchyNode node : nodes) {
			if (node instanceof VisualPlace)
				lockedPlaces.add((VisualPlace)node);
			else if (node instanceof VisualGroup)
				lockPlaces( ((VisualGroup)node).getChildren());
		}
	}

	@Override
	protected void removeNodes(Collection<HierarchyNode> nodes) {
		lockPlaces(nodes);
		super.removeNodes(nodes);
		refreshImplicitPlaces();
		lockedPlaces.clear();
	}
}
