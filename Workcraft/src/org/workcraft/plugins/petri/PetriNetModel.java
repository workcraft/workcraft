package org.workcraft.plugins.petri;

import java.util.Collection;

import org.workcraft.dom.Model;

public interface PetriNetModel extends Model {
	public Collection<Transition> getTransitions();
	public Collection<Place> getPlaces();

	public Place createPlace(String name);
	public Place createPlace();

	public Transition createDummyTransition(String name);
	public Transition createTransition();

	public boolean isEnabled (Transition t);
	public void fire (Transition t);
}