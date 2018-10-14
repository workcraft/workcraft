package org.workcraft.plugins.petri;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.MultiSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PetriNet extends AbstractMathModel implements PetriNetModel {

    public PetriNet() {
        this(null, null);
    }

    public PetriNet(Container root, References refs) {
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
    public final Collection<Connection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), Connection.class);
    }

    @Override
    public boolean isUnfireEnabled(Transition t) {
        return isUnfireEnabled(this, t);
    }

    @Override
    public final boolean isEnabled(Transition t) {
        return isEnabled(this, t);
    }

    public static final boolean isUnfireEnabled(PetriNetModel net, Transition t) {
        // gather number of connections for each post-place
        Map<Place, Integer> map = new HashMap<>();
        for (Connection c: net.getConnections(t)) {
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

    public static final boolean isEnabled(PetriNetModel net, Transition t) {
        // gather number of connections for each pre-place
        Map<Place, Integer> map = new HashMap<>();
        for (Connection c: net.getConnections(t)) {
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

    @Override
    public final void unFire(Transition t) {
        unFire(this, t);
    }

    public static final void unFire(PetriNetModel net, Transition t) {
        // the opposite action to fire, no additional checks,
        // (the transition must be "unfireble")

        // first consume tokens and then produce tokens (to avoid extra capacity)
        for (Connection c : net.getConnections(t)) {
            if (t == c.getFirst()) {
                Place to = (Place) c.getSecond();
                to.setTokens(to.getTokens() - 1);
            }
        }
        for (Connection c : net.getConnections(t)) {
            if (t == c.getSecond()) {
                Place from = (Place) c.getFirst();
                from.setTokens(from.getTokens() + 1);
            }
        }
    }

    public static final void fire(PetriNetModel net, Transition t) {
        if (net.isEnabled(t)) {
            // first consume tokens and then produce tokens (to avoid extra capacity)
            for (Connection c : net.getConnections(t)) {
                if (t == c.getSecond()) {
                    Place from = (Place) c.getFirst();
                    from.setTokens(from.getTokens() - 1);
                }
            }
            for (Connection c : net.getConnections(t)) {
                if (t == c.getFirst()) {
                    Place to = (Place) c.getSecond();
                    to.setTokens(to.getTokens() + 1);
                }
            }
        }
    }

    public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
        if (first instanceof Place && second instanceof Place) {
            throw new InvalidConnectionException("Connections between places are not valid");
        }
        if (first instanceof Transition && second instanceof Transition) {
            throw new InvalidConnectionException("Connections between transitions are not valid");
        }

        MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
        Hierarchy.getNearestContainer(first, second).add(con);
        return con;
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
