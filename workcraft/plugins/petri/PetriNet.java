package org.workcraft.plugins.petri;

import java.util.ArrayList;
import java.util.LinkedList;

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
	protected LinkedList<Place> places = new LinkedList<Place>();
	protected LinkedList<Transition> transitions = new LinkedList<Transition>();

	public PetriNet(Framework framework) {
		super(framework);
	}

	public PetriNet(Framework framework, Element xmlElement, String sourcePath) throws ModelLoadFailedException {
		super(framework, xmlElement, sourcePath);
	}



	@Override
	public int addComponent(Component component, boolean autoAssignID)
	throws InvalidComponentException, DuplicateIDException {
		int id = super.addComponent(component, autoAssignID);

		if (component instanceof Place)
			places.add((Place)component);
		else if (component instanceof Transition)
			transitions.add((Transition)component);

		return id;
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
	protected void validateConnection(Connection connection)
	throws InvalidConnectionException {
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
}
