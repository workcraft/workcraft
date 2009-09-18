package org.workcraft.plugins.petri;

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.util.Hierarchy;

@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
@DisplayName("Petri Net")
public class PetriNet extends AbstractMathModel {

	public PetriNet() {
		this(null);
	}

	public PetriNet(Container root) {
		super(root);
		new DefaultHangingConnectionRemover(this).attach(getRoot());
	}

	public void validate() throws ModelValidationException {
	}

	final public Place createPlace() {
		Place newPlace = new Place();
		getRoot().add(newPlace);
		return newPlace;
	}

	final public Transition createTransition() {
		Transition newTransition = new Transition();
		getRoot().add(newTransition);
		return newTransition;
	}

	final public Collection<Place> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	final public boolean isEnabled (Transition t) {
		for (Node n : getPreset(t))
			if (((Place)n).getTokens() <= 0)
				return false;
		return true;
	}

	final public void fire (Transition t) {
		if (isEnabled(t))
		{
			for (Node n : getPostset(t))
				((Place)n).setTokens(((Place)n).getTokens()+1);
			for (Node n : getPreset(t))
				((Place)n).setTokens(((Place)n).getTokens()-1);
		}
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (first instanceof Transition && second instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}
}
