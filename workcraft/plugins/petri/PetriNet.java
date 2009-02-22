package org.workcraft.plugins.petri;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Petri Net")
@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
public class PetriNet extends MathModel {
	private HashSet<Place> places = new HashSet<Place>();
	private HashSet<Transition> transitions = new HashSet<Transition>();

	public PetriNet() {
		super();
		addSupportedComponents();
	}

	private void addSupportedComponents() {
		addComponentSupport(Place.class);
		addComponentSupport(Transition.class);
	}

	protected void onComponentAdded(Component component) {
		if (component instanceof Place)
			places.add((Place)component);
		else if (component instanceof Transition)
			transitions.add((Transition)component);
	}

	protected void onComponentRemoved (Component component) {
		if (component instanceof Place)
			places.remove(component);
		else if (component instanceof Transition)
			transitions.remove(component);
	}

	public void validate() throws ModelValidationException {
	}


	public void validateConnection(Connection connection)	throws InvalidConnectionException {
		if (connection.getFirst() instanceof Place && connection.getSecond() instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (connection.getFirst() instanceof Transition && connection.getSecond() instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}

	final public Place createPlace(String label) {
		Place newPlace = new Place();
		newPlace.setLabel(label);
		addComponent(newPlace);
		return newPlace;
	}

	final public Transition createTransition(String label) {
		Transition newTransition = new Transition();
		newTransition.setLabel(label);
		addComponent(newTransition);
		return newTransition;
	}

	final public Set<Place> getPlaces() {
		return new HashSet<Place>(places);
	}

	final public Set<Transition> getTransitions() {
		return new HashSet<Transition>(transitions);
	}

	public void onConnectionAdded(Connection connection) {
	}

	public void onConnectionRemoved(Connection connection) {
	}
}
