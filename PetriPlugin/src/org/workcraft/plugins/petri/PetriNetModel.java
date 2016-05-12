package org.workcraft.plugins.petri;

import java.util.Collection;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;

public interface PetriNetModel extends Model {
    Collection<Transition> getTransitions();
    Collection<Place> getPlaces();
    Collection<Connection> getConnections();

    boolean isEnabled(Transition t);
    void fire(Transition t);

    boolean isUnfireEnabled(Transition t);
    void unFire(Transition t);
}
