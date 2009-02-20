package org.workcraft.plugins.stg;

import java.awt.geom.Point2D;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualConnection;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModelEventListener;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;

public class VisualSTG extends VisualPetriNet implements MathModelListener {
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

		public void onSelectionChanged() {
		}
	}

	private HashSet<VisualSignalTransition> transitions = new HashSet<VisualSignalTransition>();

	@Override
	public void validateConnection(VisualNode first, VisualNode second)
	throws InvalidConnectionException {
		if (first instanceof VisualPlace) {
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places are not allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Connections between places and implicit places are not allowed");
		}

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualConnection)
				if (! (second  instanceof STGConnection))
					throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
		}

		if (first instanceof VisualConnection) {
			if (!(first instanceof STGConnection))
				throw new InvalidConnectionException ("Only connections with arcs having implicit places are allowed");
			if (second instanceof VisualConnection)
				throw new InvalidConnectionException ("Connections between arcs are not allowed");
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places and implicit places are not allowed");

			STGConnection con = (STGConnection) first;
			if (con.getFirst() == second || con.getSecond() == second)
				throw new InvalidConnectionException ("Arc already exists");
		}
	}

	@Override
	public VisualConnection connect(VisualNode first,
			VisualNode second) throws InvalidConnectionException {

		validateConnection(first, second);

		if (first instanceof VisualSignalTransition) {
			if (second instanceof VisualSignalTransition) {
				STG mathModel = (STG)getMathModel();
				VisualSignalTransition t1 = (VisualSignalTransition) first;
				VisualSignalTransition t2 = (VisualSignalTransition) second;

				Place implicitPlace = mathModel.createPlace("");
				Connection con1 = mathModel.connect(t1.getReferencedTransition(), implicitPlace);
				Connection con2 = mathModel.connect(implicitPlace, t2.getReferencedTransition());

				STGConnection connection = new STGConnection((VisualComponent)first, (VisualComponent)second, con1, con2, implicitPlace);

				VisualGroup group = VisualNode.getCommonParent(first, second);

				group.add(connection);
				addConnection(connection);

				return connection;
			} else if (second instanceof STGConnection) {
				STGConnection con = (STGConnection)second;
				VisualGroup group = con.getParent();

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());

				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());

				addComponent(place);
				addConnection(con1);
				addConnection(con2);
				group.add(place);
				group.add(con1);
				group.add(con2);

				removeVisualConnectionOnly(con);

				return super.connect(first, place);
			}
		}

		if (first instanceof STGConnection)
			if (second instanceof VisualSignalTransition) {
				STGConnection con = (STGConnection)first;
				VisualGroup group = con.getParent();

				Place implicitPlace = con.getImplicitPlace();

				VisualPlace place = new VisualPlace(implicitPlace);
				Point2D p = con.getPointOnConnection(0.5);
				place.setX(p.getX()); place.setY(p.getY());

				VisualConnection con1 = new VisualConnection(con.getRefCon1(), con.getFirst(), place);
				VisualConnection con2 = new VisualConnection(con.getRefCon2(), place, con.getSecond());

				addComponent(place);
				addConnection(con1);
				addConnection(con2);
				group.add(place);
				group.add(con1);
				group.add(con2);

				removeVisualConnectionOnly(con);

				return super.connect(place, second);

			}



		return super.connect(first, second);
	}

	private void removeVisualConnectionOnly(VisualConnection connection) {
		connection.getParent().remove(connection);
		selection().remove(connection);
		connection.removeListener(getPropertyChangeListener());
	}

	@Override
	protected void removeConnection(VisualConnection connection) {
		if (connection instanceof STGConnection) {
			connection.getFirst().removeConnection(connection);
			connection.getSecond().removeConnection(connection);

			getMathModel().removeConnection(((STGConnection) connection).getRefCon1());
			getMathModel().removeConnection(((STGConnection) connection).getRefCon2());
			getMathModel().removeComponent(((STGConnection) connection).getImplicitPlace());

			connection.getParent().remove(connection);
			selection().remove(connection);

			connection.removeListener(getPropertyChangeListener());

			fireConnectionRemoved(connection);

		} else {
			super.removeConnection(connection);
		}
	}

	public VisualSTG(STG model) throws VisualModelInstantiationException {
		super(model);
		addListener(new Listener());
	}

	public VisualSTG(STG model, Element element) throws VisualModelInstantiationException {
		super(model, element);
		addListener(new Listener());
	}

	public void onComponentPropertyChanged(Component c) {
		System.out.println (c);
	}

	public void onConnectionPropertyChanged(Connection c) {
		System.out.println (c);
	}

	public void onModelStructureChanged() {
		System.out.println ("Structure changed");
	}

}
