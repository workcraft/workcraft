package org.workcraft.plugins.petri;

import java.util.Collection;

import org.workcraft.dom.Model;

public interface PetriNetModel extends Model {
	public Collection<Transition> getTransitions();
	public Collection<Place> getPlaces();

	public boolean isEnabled (Transition t);
	public void fire (Transition t);

	public boolean isUnfireEnabled (Transition t);
	public void unFire(Transition t);
}