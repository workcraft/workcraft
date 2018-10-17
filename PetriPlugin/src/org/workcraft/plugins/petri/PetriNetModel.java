package org.workcraft.plugins.petri;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;

import java.util.Collection;

public interface PetriNetModel extends MathModel {
    Collection<Transition> getTransitions();
    Collection<Place> getPlaces();
    Collection<MathConnection> getConnections();

    boolean isEnabled(Transition t);
    void fire(Transition t);

    boolean isUnfireEnabled(Transition t);
    void unFire(Transition t);
}
