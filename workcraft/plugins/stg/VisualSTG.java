package org.workcraft.plugins.stg;

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
	public void validateConnection(VisualComponent first, VisualComponent second)
			throws InvalidConnectionException {
		if (first instanceof VisualPlace)
			if (second instanceof VisualPlace)
				throw new InvalidConnectionException ("Connections between places are not allowed");

	}

	@Override
	public VisualConnection connect(VisualComponent first,
			VisualComponent second) throws InvalidConnectionException {
		if (first instanceof VisualSignalTransition && second instanceof VisualSignalTransition) {
			STG mathModel = (STG)getMathModel();
			VisualSignalTransition t1 = (VisualSignalTransition) first;
			VisualSignalTransition t2 = (VisualSignalTransition) second;

			Place implicitPlace = mathModel.createPlace("");
			Connection con1 = mathModel.connect(t1.getReferencedTransition(), implicitPlace);
			Connection con2 = mathModel.connect(implicitPlace, t2.getReferencedTransition());

			STGConnection connection = new STGConnection(first, second, con1, con2, implicitPlace);

			VisualGroup group = VisualNode.getCommonParent(first, second);

			group.add(connection);
			addConnection(connection);

			return connection;
		} else {
			return super.connect(first, second);
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
