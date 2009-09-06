package org.workcraft.plugins.petri;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

@VisualClass ("org.workcraft.plugins.petri.VisualPetriNet")
public class PetriNet extends AbstractMathModel {
	private HashSet<Place> places = new HashSet<Place>();
	private HashSet<Transition> transitions = new HashSet<Transition>();

	public PetriNet() {
		super();

		addObserver(new HierarchyObserver() {
			public void notify(HierarchyEvent e) {
				if (e instanceof NodesAddedEvent) {
					for (Node n : e.getAffectedNodes())
						if (n instanceof Place) places.add((Place)n);
						else if (n instanceof Transition) transitions.add((Transition)n);
				} else if (e instanceof NodesDeletedEvent) {
					for (Node n : e.getAffectedNodes())
						if (n instanceof Place) places.remove((Place)n);
						else if (n instanceof Transition) transitions.remove((Transition)n);
				}
			}
		});
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

	final public Set<Place> getPlaces() {
		return new HashSet<Place>(places);
	}

	final public Set<Transition> getTransitions() {
		return new HashSet<Transition>(transitions);
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
