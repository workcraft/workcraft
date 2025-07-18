package org.workcraft.plugins.petri;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.serialisation.References;
import org.workcraft.types.MultiSet;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Petri extends AbstractMathModel implements PetriModel {

    public Petri() {
        this(null, null);
    }

    public Petri(Container root, References refs) {
        super(root, refs);
    }

    public final Place createPlace(String name, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Place place = new Place();
        container.add(place);
        if (name != null) {
            setName(place, name);
        }
        return place;
    }

    public final Transition createTransition(String name, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Transition transition = new Transition();
        container.add(transition);
        if (name != null) {
            setName(transition, name);
        }
        return transition;
    }

    @Override
    public final Collection<Place> getPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
    }

    @Override
    public final Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    @Override
    public final Collection<MathConnection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), MathConnection.class);
    }

    @Override
    public final boolean isEnabled(Transition t) {
        return isEnabled(this, t);
    }

    public static boolean isEnabled(PetriModel net, Transition t) {
        // gather number of connections for each pre-place
        Map<Place, Integer> map = new HashMap<>();
        for (MathConnection c: net.getConnections(t)) {
            if (c.getSecond() == t) {
                Place p = (Place) c.getFirst();
                if (map.containsKey(p)) {
                    map.put(p, map.get(p) + 1);
                } else {
                    map.put(p, 1);
                }
            }
        }
        for (Node n: net.getPreset(t)) {
            Place p = (Place) n;
            if (p.getTokens() < map.get(p)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final void fire(Transition t) {
        fire(this, t);
    }

    public static void fire(PetriModel net, Transition t) {
        if (net.isEnabled(t)) {
            // first consume tokens and then produce tokens (to avoid extra capacity)
            for (MathConnection c : net.getConnections(t)) {
                if (t == c.getSecond()) {
                    Place from = (Place) c.getFirst();
                    from.setTokens(from.getTokens() - 1);
                }
            }
            for (MathConnection c : net.getConnections(t)) {
                if (t == c.getFirst()) {
                    Place to = (Place) c.getSecond();
                    to.setTokens(to.getTokens() + 1);
                }
            }
        }
    }

    @Override
    public boolean isUnfireEnabled(Transition t) {
        return isUnfireEnabled(this, t);
    }

    public static boolean isUnfireEnabled(PetriModel net, Transition t) {
        // gather number of connections for each post-place
        Map<Place, Integer> map = new HashMap<>();
        for (MathConnection c: net.getConnections(t)) {
            if (c.getFirst() == t) {
                if (map.containsKey(c.getSecond())) {
                    map.put((Place) c.getSecond(), map.get(c.getSecond()) + 1);
                } else {
                    map.put((Place) c.getSecond(), 1);
                }
            }
        }
        for (Node n : net.getPostset(t)) {
            if (((Place) n).getTokens() < map.get((Place) n)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final void unFire(Transition t) {
        unFire(this, t);
    }

    public static void unFire(PetriModel net, Transition t) {
        // the opposite action to fire, no additional checks,
        // (the transition must be "unfireble")

        // first consume tokens and then produce tokens (to avoid extra capacity)
        for (MathConnection c : net.getConnections(t)) {
            if (t == c.getFirst()) {
                Place to = (Place) c.getSecond();
                to.setTokens(to.getTokens() - 1);
            }
        }
        for (MathConnection c : net.getConnections(t)) {
            if (t == c.getSecond()) {
                Place from = (Place) c.getFirst();
                from.setTokens(from.getTokens() + 1);
            }
        }
    }

    @Override
    public void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException {
        super.validateConnection(first, second);

        if ((first instanceof Place) && (second instanceof Place)) {
            throw new InvalidConnectionException("Connections between places are not allowed");
        }

        if ((first instanceof Transition) && (second instanceof Transition)) {
            throw new InvalidConnectionException("Connections between transitions are not allowed");
        }
    }

    @Override
    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        result.add("Place", getPlaces().size());
        result.add("Transition", getTransitions().size());
        result.add("Arc", getConnections().size());
        return result;
    }

}
