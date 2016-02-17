/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.petri;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

@VisualClass (org.workcraft.plugins.petri.VisualPetriNet.class)
public class PetriNet extends AbstractMathModel implements PetriNetModel {

    public PetriNet() {
        this(null, (References) null);
    }

    public PetriNet(Container root, References refs) {
        this(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof Place) return "p";
                if (node instanceof Transition) return "t";
                return super.getPrefix(node);
            }
        });
    }

    public PetriNet(Container root, ReferenceManager man) {
        super(root, man);
    }

    final public Place createPlace(String name, Container container) {
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

    final public Transition createTransition(String name, Container container) {
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
    final public Collection<Place> getPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
    }

    @Override
    final public Collection<Transition> getTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
    }

    @Override
    final public Collection<Connection> getConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), Connection.class);
    }

    @Override
    public boolean isUnfireEnabled(Transition t) {
        return isUnfireEnabled(this, t);
    }

    @Override
    final public boolean isEnabled(Transition t) {
        return isEnabled(this, t);
    }

    final public static boolean isUnfireEnabled(PetriNetModel net, Transition t) {
        // gather number of connections for each post-place
        Map<Place, Integer> map = new HashMap<Place, Integer>();
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

    final public static boolean isEnabled(PetriNetModel net, Transition t) {
        // gather number of connections for each pre-place
        Map<Place, Integer> map = new HashMap<Place, Integer>();
        for (Connection c: net.getConnections(t)) {
            if (c.getSecond() == t) {
                if (map.containsKey(c.getFirst())) {
                    map.put((Place) c.getFirst(), map.get(c.getFirst()) + 1);
                } else {
                    map.put((Place) c.getFirst(), 1);
                }
            }
        }
        for (Node n : net.getPreset(t)) {
            if (((Place) n).getTokens() < map.get((Place) n)) {
                return false;
            }
        }
        return true;
    }

    @Override
    final public void fire(Transition t) {
        fire(this, t);
    }

    @Override
    final public void unFire(Transition t) {
        unFire(this, t);
    }

    final public static void unFire(PetriNetModel net, Transition t) {
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

    final public static void fire(PetriNetModel net, Transition t) {
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

}
