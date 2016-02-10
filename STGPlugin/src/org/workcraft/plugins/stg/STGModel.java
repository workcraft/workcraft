package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.Set;

import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Type;

public interface STGModel extends PetriNetModel {
    Collection<Transition> getDummyTransitions();
    Collection<SignalTransition> getSignalTransitions();

    Set<String> getDummyReferences();
    Set<String> getSignalReferences(Type type);
}
