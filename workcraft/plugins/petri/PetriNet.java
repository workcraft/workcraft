package org.workcraft.plugins.petri;

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

@DisplayName ("Petri Net")
@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
public class PetriNet extends MathModel {

	public class Listener implements MathModelListener {
		public void onComponentAdded(Component component) {
			if (component instanceof Place)
				places.add((Place)component);
			else if (component instanceof Transition)
				transitions.add((Transition)component);
		}

		public void onComponentRemoved(Component component) {
			if (component instanceof Place)
				places.remove(component);
			else if (component instanceof Transition)
				transitions.remove(component);
		}

		public void onConnectionAdded(Connection connection) {
		}

		public void onConnectionRemoved(Connection connection) {
		}

		public void onNodePropertyChanged(String propertyName, MathNode n) {
		}
	}

	private HashSet<Place> places = new HashSet<Place>();
	private HashSet<Transition> transitions = new HashSet<Transition>();

	public PetriNet() {
		super();
		addSupportedComponents();
		addListener(new Listener());
	}

	private void addSupportedComponents() {
		addComponentSupport(Place.class);
		addComponentSupport(Transition.class);
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
		if (connection.getFirst() instanceof Place && connection.getSecond() instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (connection.getFirst() instanceof Transition && connection.getSecond() instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}

	final public Place createPlace() {
		Place newPlace = new Place();
		addComponent(newPlace);
		return newPlace;
	}

	final public Transition createTransition() {
		Transition newTransition = new Transition();
		addComponent(newTransition);
		return newTransition;
	}

	final public Set<Place> getPlaces() {
		return new HashSet<Place>(places);
	}

	final public Set<Transition> getTransitions() {
		return new HashSet<Transition>(transitions);
	}
}
