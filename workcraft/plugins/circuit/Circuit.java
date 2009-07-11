package org.workcraft.plugins.circuit;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.MathModelListener;
import org.workcraft.dom.MathNode;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Digital Circuit")
@VisualClass ("org.workcraft.plugins.circuit.VisualCircuit")

public class Circuit extends MathModel {


	public class Listener implements MathModelListener {

		public void onComponentAdded(Component component) {
			if (component instanceof Formula)
				components.add((CircuitComponent)component);
			else if (component instanceof Joint)
				components.add((Joint)component);
			else if (component instanceof Contact)
				components.add((Contact)component);
		}

		public void onComponentRemoved(Component component) {
			if (component instanceof Formula)
				components.remove(component);
			else if (component instanceof Joint)
				components.remove(component);
			else if (component instanceof Contact)
				components.remove(component);
		}

		public void onConnectionAdded(Connection connection) {
		}

		public void onConnectionRemoved(Connection connection) {
		}

		public void onNodePropertyChanged(String propertyName, MathNode n) {
		}
	}


	private HashSet<Component> components = new HashSet<Component>();

	public Circuit() {
		super();
		addSupportedComponents();
		addListener(new Listener());
	}

	private void addSupportedComponents() {
		addComponentSupport(Formula.class);
		addComponentSupport(Joint.class);
		addComponentSupport(Contact.class);
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
/*		if (connection.getFirst() instanceof Place && connection.getSecond() instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (connection.getFirst() instanceof Transition && connection.getSecond() instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");*/

	}

//	final public Place createPlace() {
//		Place newPlace = new Place();
//		addComponent(newPlace);
//		return newPlace;
//	}
//
//	final public Transition createTransition() {
//		Transition newTransition = new Transition();
//		addComponent(newTransition);
//		return newTransition;
//	}

	final public Set<Component> getCircuitComponenets() {
		return new HashSet<Component>(components);
	}

}
