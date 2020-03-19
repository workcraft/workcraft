package org.workcraft.plugins.petri;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;

import java.util.Collection;

public interface PetriModel extends MathModel {
    Collection<? extends Transition> getTransitions();
    Collection<? extends Place> getPlaces();
    Collection<? extends MathConnection> getConnections();

    boolean isEnabled(Transition t);
    void fire(Transition t);

    boolean isUnfireEnabled(Transition t);
    void unFire(Transition t);
}
