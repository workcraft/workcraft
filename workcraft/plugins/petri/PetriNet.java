package org.workcraft.plugins.petri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.MathModel;
import org.workcraft.dom.VisualClass;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.DuplicateIDException;
import org.workcraft.framework.exceptions.InvalidComponentException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelLoadFailedException;
import org.workcraft.framework.exceptions.ModelValidationException;

@DisplayName ("Petri Net")
@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
public class PetriNet extends MathModel {
	protected HashSet<Place> places;
	protected HashSet<Transition> transitions;

	public PetriNet(Framework framework) {
		super(framework);
	}

	public PetriNet(Framework framework, Element xmlElement, String sourcePath) throws ModelLoadFailedException {
		super(framework, xmlElement, sourcePath);
	}

	@Override
	protected void componentAdded(Component component) {
		if (component instanceof Place)
			getPlacesSet().add((Place)component);
		else if (component instanceof Transition)
			getTransitionsSet().add((Transition)component);
	}

	@Override
	protected void componentRemoved (Component component) {
		if (component instanceof Place)
			getPlacesSet().remove(component);
		else if (component instanceof Transition)
			getTransitionsSet().remove(component);
	}


	@Override
	public ArrayList<Class<? extends Component>> getSupportedComponents() {
		ArrayList<Class<? extends Component>> list = new ArrayList<Class<? extends Component>>(super.getSupportedComponents());
		list.add(Place.class);
		list.add(Transition.class);
		return list;
	}


	@Override
	public void validate() throws ModelValidationException {
		// TODO Auto-generated method stub

	}


	@Override
	public void validateConnection(Connection connection)	throws InvalidConnectionException {
		if (connection.getFirst() instanceof Place && connection.getSecond() instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (connection.getFirst() instanceof Transition && connection.getSecond() instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}

	public Place createPlace(String label) {
		Place newPlace = new Place();
		newPlace.setLabel(label);
		try {
			addComponent(newPlace, true);
		} catch (InvalidComponentException e) {
			e.printStackTrace();
		} catch (DuplicateIDException e) {
			e.printStackTrace();
		}

		return newPlace;
	}

	public Transition createTransition(String label) {
		Transition newTransition = new Transition();
		newTransition.setLabel(label);
		try {
			addComponent(newTransition, true);
		} catch (InvalidComponentException e) {
			e.printStackTrace();
		} catch (DuplicateIDException e) {
			e.printStackTrace();
		}

		return newTransition;
	}

	private Set<Place> getPlacesSet() {
		if (places == null)
			places = new HashSet<Place>();
		return places;
	}

	private Set<Transition> getTransitionsSet() {
		if (transitions == null)
			transitions = new HashSet<Transition>();
		return transitions;
	}
}
