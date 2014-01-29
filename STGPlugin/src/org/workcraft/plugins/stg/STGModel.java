package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.Set;

import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Type;

public interface STGModel extends PetriNetModel {
	public SignalTransition createSignalTransition (String name);

	public Collection<SignalTransition> getSignalTransitions();
	public Collection<SignalTransition> getSignalTransitions(Type type);

	public Collection<Transition> getDummies();

	public Set<String> getDummyNames();
	public Set<String> getSignalNames(Type type);
}
