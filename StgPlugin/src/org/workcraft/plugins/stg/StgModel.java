package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.Set;

import org.workcraft.plugins.petri.PetriModel;

public interface StgModel extends PetriModel {
    Collection<DummyTransition> getDummyTransitions();
    Collection<SignalTransition> getSignalTransitions();
    Collection<SignalTransition> getSignalTransitions(Signal.Type type);
    Collection<StgPlace> getMutexPlaces();

    Set<String> getDummyReferences();
    Set<String> getSignalReferences();
    Set<String> getSignalReferences(Signal.Type type);
    int getInstanceNumber(NamedTransition nt);
    void setInstanceNumber(NamedTransition nt, int number);

    String getSignalReference(SignalTransition st);
}
