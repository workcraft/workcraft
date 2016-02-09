package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.Set;

import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Type;

public interface STGModel extends PetriNetModel {
    public Collection<Transition> getDummyTransitions();
    public Collection<SignalTransition> getSignalTransitions();

    public Set<String> getDummyReferences();
    public Set<String> getSignalReferences(Type type);
}
