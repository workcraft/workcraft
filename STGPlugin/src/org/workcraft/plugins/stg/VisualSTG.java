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

package org.workcraft.plugins.stg;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.propertydescriptors.SignalNamePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;

@DisplayName("Signal Transition Graph")
@CustomTools(STGToolsProvider.class)
public class VisualSTG extends AbstractVisualModel {

    public VisualSTG() {
        this(new STG(), null);
    }

    public VisualSTG(STG model) {
        this(model, null);
    }

    public VisualSTG(STG model, VisualGroup root) {
        super(model, root);
        if (root == null) {
            try {
                createDefaultFlatStructure();
                fixReadArcs();
                // FIXME: Implicit places should not appear in the first place.
                fixVisibilityOfImplicitPlaces();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fixVisibilityOfImplicitPlaces() {
        for (VisualPlace vp: getVisualPlaces()) {
            Place p = vp.getReferencedPlace();
            if (p instanceof STGPlace) {
                STGPlace pp = (STGPlace)p;
                if (pp.isImplicit()) {
                    maybeMakeImplicit(vp, false);
                }
            }
        }
    }

    private void fixReadArcs() {
        HashSet<Pair<VisualConnection, VisualConnection>> dualArcs = PetriNetUtils.getSelectedOrAllDualArcs(this);
        PetriNetUtils.convertDualArcsToReadArcs(this, dualArcs);
    }

    @Override
    public void validateConnection(Node first, Node second)    throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
        if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace) || (first instanceof VisualImplicitPlaceArc))
                && ((second instanceof VisualPlace) || (second instanceof VisualReplicaPlace) || (second instanceof VisualImplicitPlaceArc))) {
            throw new InvalidConnectionException("Arcs between places are not allowed.");
        }
        if (PetriNetUtils.hasReadArcConnection(this, first, second) || PetriNetUtils.hasReadArcConnection(this, second, first)) {
            throw new InvalidConnectionException("Nodes are already connected by a read-arc.");
        }
        if (PetriNetUtils.hasProducingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This producing arc already exists.");
        }
        if (PetriNetUtils.hasConsumingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This consuming arc already exists.");
        }
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualConnection connection = null;
        if (first instanceof VisualTransition) {
            if (second instanceof VisualTransition) {
                connection = createImplicitPlaceConnection((VisualTransition)first, (VisualTransition)second);
            } else if (second instanceof VisualImplicitPlaceArc) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc)second;
                VisualPlace place = makeExplicit(con);
                connection = connect(first, place);
            } else if ((second instanceof VisualPlace) || (second instanceof VisualReplicaPlace)) {
                connection = createSimpleConnection((VisualNode)first, (VisualNode)second, mConnection);
            }
        } else if (first instanceof VisualImplicitPlaceArc) {
            if (second instanceof VisualTransition) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc)first;
                VisualPlace place = makeExplicit(con);
                connection = connect(place, second);
            }
        } else if ((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace)) {
            connection = createSimpleConnection((VisualNode)first, (VisualNode)second, mConnection);
        }
        return connection;
    }

    private VisualImplicitPlaceArc createImplicitPlaceConnection(VisualTransition t1, VisualTransition t2) throws InvalidConnectionException {
        STG stg = (STG)getMathModel();
        final ConnectionResult connectResult = stg.connect(t1.getReferencedTransition(), t2.getReferencedTransition());

        STGPlace implicitPlace = connectResult.getImplicitPlace();
        MathConnection con1 = connectResult.getCon1();
        MathConnection con2 = connectResult.getCon2();

        if (implicitPlace == null || con1 == null || con2 == null) {
            throw new NullPointerException();
        }
        VisualImplicitPlaceArc connection = new VisualImplicitPlaceArc(t1, t2, con1, con2, implicitPlace);
        Hierarchy.getNearestContainer(t1, t2).add(connection);
        return connection;
    }

    private VisualConnection createSimpleConnection(final VisualNode first, final VisualNode second,
            MathConnection mConnection) throws InvalidConnectionException {

        STG stg = (STG)getMathModel();
        if (mConnection == null) {
            MathNode firstRef = getMathReference(first);
            MathNode secondRef = getMathReference(second);
            ConnectionResult result = stg.connect(firstRef, secondRef);
            mConnection = result.getSimpleResult();
        }
        VisualConnection connection = new VisualConnection(mConnection, first, second);
        Hierarchy.getNearestContainer(first, second).add(connection);
        return connection;
    }

    @Override
    public void validateUndirectedConnection(Node first, Node second)    throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
        if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace))
                && ((second instanceof VisualPlace) || (second instanceof VisualReplicaPlace))) {
            throw new InvalidConnectionException("Read-arcs between places are not allowed.");
        }
        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) {
            throw new InvalidConnectionException("Read-arcs between transitions are not allowed.");
        }
        if (PetriNetUtils.hasReadArcConnection(this, first, second)
                || PetriNetUtils.hasReadArcConnection(this, second, first)
                || PetriNetUtils.hasProducingArcConnection(this, first, second)
                || PetriNetUtils.hasProducingArcConnection(this, second, first)
                || PetriNetUtils.hasConsumingArcConnection(this, first, second)
                || PetriNetUtils.hasConsumingArcConnection(this, second, first)) {
            throw new InvalidConnectionException("Nodes are already connected.");
        }
    }

    @Override
    public VisualConnection connectUndirected(Node first, Node second) throws InvalidConnectionException {
        validateUndirectedConnection(first, second);

        VisualNode place = null;
        VisualNode transition = null;
        if (first instanceof VisualTransition) {
            place = (VisualNode)second;
            transition = (VisualNode)first;
        } else if (second instanceof VisualTransition) {
            place = (VisualNode)first;
            transition = (VisualNode)second;
        }
        VisualConnection connection = null;
        if ((place != null) && (transition != null)) {
            connection = createReadArcConnection(place, transition);
        }
        return connection;
    }

    private VisualReadArc createReadArcConnection(VisualNode place, VisualNode transition)
             throws InvalidConnectionException {
        STG stg = (STG)getMathModel();

        Place mPlace = null;
        if (place instanceof VisualPlace) {
            mPlace = ((VisualPlace)place).getReferencedPlace();
        } else if (place instanceof VisualReplicaPlace) {
            mPlace = ((VisualReplicaPlace)place).getReferencedPlace();
        }
        Transition mTransition = null;
        if (transition instanceof VisualTransition) {
            mTransition = ((VisualTransition)transition).getReferencedTransition();
        }

        VisualReadArc connection = null;
        if ((mPlace != null) && (mTransition !=null)) {
            MathConnection mConsumingConnection = stg.connect(mPlace, mTransition).getSimpleResult();
            MathConnection mProducingConnection = stg.connect(mTransition, mPlace).getSimpleResult();

            connection = new VisualReadArc(place, transition, mConsumingConnection, mProducingConnection);
            Hierarchy.getNearestContainer(place, transition).add(connection);
        }
        return connection;
    }

    public VisualPlace makeExplicit(VisualImplicitPlaceArc connection) {
        Container group = Hierarchy.getNearestAncestor(connection, Container.class);
        STG stg = (STG)getMathModel();
        Point2D splitPoint = connection.getSplitPoint();
        STGPlace implicitPlace = connection.getImplicitPlace();
        stg.makeExplicit(implicitPlace);
        VisualPlace place = new VisualPlace(implicitPlace);
        place.setPosition(splitPoint);

        VisualConnection con1 = new VisualConnection(connection.getRefCon1(), connection.getFirst(), place);
        VisualConnection con2 = new VisualConnection(connection.getRefCon2(), place, connection.getSecond());

        group.add(place);
        group.add(con1);
        group.add(con2);

        LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPoint);
        ConnectionHelper.addControlPoints(con1, prefixLocationsInRootSpace);
        LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPoint);
        ConnectionHelper.addControlPoints(con2, suffixLocationsInRootSpace);

        con1.copyStyle(connection);
        con2.copyStyle(connection);

        remove(connection);
        return place;
    }

    public void maybeMakeImplicit(VisualPlace place, boolean preserveConnectionShape) {
        Collection<Node> preset = getPreset(place);
        Collection<Node> postset = getPostset(place);
        Collection<Replica> replicas = place.getReplicas();
        if ((preset.size() == 1) && (postset.size() == 1) && replicas.isEmpty()) {
            final STGPlace stgPlace = (STGPlace)place.getReferencedPlace();
            stgPlace.setImplicit(true);
            VisualComponent first = (VisualComponent)preset.iterator().next();
            VisualComponent second = (VisualComponent)postset.iterator().next();

            VisualConnection con1 = null;
            VisualConnection con2 = null;
            Collection<Connection> connections = new ArrayList<Connection>(getConnections(place));
            for (Connection con: connections) {
                if (con.getFirst() == place) {
                    con2 = (VisualConnection)con;
                } else if (con.getSecond() == place) {
                    con1 = (VisualConnection)con;
                }
            }
            MathConnection refCon1 = con1.getReferencedConnection();
            MathConnection refCon2 = con2.getReferencedConnection();
            VisualImplicitPlaceArc connection = new VisualImplicitPlaceArc(first, second, refCon1, refCon2, (STGPlace)place.getReferencedPlace());
            Container parent = Hierarchy.getNearestAncestor(Hierarchy.getCommonParent(first, second), Container.class);
            parent.add(connection);
            if (preserveConnectionShape) {
                LinkedList<Point2D> locations = ConnectionHelper.getMergedControlPoints(place, con1, con2);
                ConnectionHelper.addControlPoints(connection, locations);
            }
            // Remove explicit place, all its connections will get removed automatically by the hanging connection remover
            remove(place);
        }
    }

    public VisualPlace createPlace(String mathName, Container container) {
        STG stg = (STG)getMathModel();
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        STGPlace mathPlace = stg.createPlace(mathName, mathContainer);
        return createVisualComponent(mathPlace, container, VisualPlace.class);
    }

    public VisualDummyTransition createDummyTransition(String mathName, Container container) {
        STG stg = (STG)getMathModel();
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        DummyTransition mathTransition = stg.createDummyTransition(mathName, mathContainer);
        return createVisualComponent(mathTransition, container, VisualDummyTransition.class);
    }

    public VisualSignalTransition createSignalTransition(String signalName, SignalTransition.Type type, Direction direction, Container container) {
        STG stg = (STG)getMathModel();
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        String mathName = null;
        if ((signalName != null) && (direction != null)) {
            mathName = signalName + direction.toString();
        }
        SignalTransition mathTransition = stg.createSignalTransition(mathName, mathContainer);
        mathTransition.setSignalType(type);
        return createVisualComponent(mathTransition, container, VisualSignalTransition.class);
    }

    public Collection<VisualPlace> getVisualPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualPlace.class);
    }

    public Collection<VisualTransition> getVisualTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualTransition.class);
    }

    public Collection<VisualSignalTransition> getVisualSignalTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualSignalTransition.class);
    }

    public Collection<VisualDummyTransition> getVisualDummyTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualDummyTransition.class);
    }

    public Collection<VisualConnection> getVisualConnections() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualConnection.class);
    }

    public HashSet<VisualConnection> getVisualConsumingArcs() {
        HashSet<VisualConnection> connections = new HashSet<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(getRoot(), VisualConnection.class)) {
            if (connection instanceof VisualReadArc) continue;
            if (connection.getSecond() instanceof VisualTransition) {
                connections.add(connection);
            }
        }
        return connections;
    }

    public HashSet<VisualConnection> getVisualProducerArcs() {
        HashSet<VisualConnection> connections = new HashSet<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(getRoot(), VisualConnection.class)) {
            if (connection instanceof VisualReadArc) continue;
            if (connection.getFirst() instanceof VisualTransition) {
                connections.add(connection);
            }
        }
        return connections;
    }

    public Collection<VisualReadArc> getVisualReadArcs() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualReadArc.class);
    }

    public Collection<VisualImplicitPlaceArc> getVisualImplicitPlaceArcs() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualImplicitPlaceArc.class);
    }

    public VisualPlace getVisualPlace(Place place) {
        for (VisualPlace vp: getVisualPlaces()) {
            if (vp.getReferencedPlace() == place) {
                return vp;
            }
        }
        return null;
    }

    public VisualTransition getVisualTransition(Transition transition) {
        for (VisualTransition vt: getVisualTransitions()) {
            if (vt.getReferencedTransition() == transition) {
                return vt;
            }
        }
        return null;
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            STG stg = (STG)getMathModel();
            for (Type type : Type.values()) {
                LinkedList<PropertyDescriptor> typeDescriptors = new LinkedList<>();
                Container container = NamespaceHelper.getMathContainer(this, getCurrentLevel());
                for (final String signalName : stg.getSignalNames(type, container)) {
                    if (stg.getSignalTransitions(signalName, container).isEmpty()) continue;
                    typeDescriptors.add(new SignalNamePropertyDescriptor(stg, signalName, container));
                    typeDescriptors.add(new SignalTypePropertyDescriptor(stg, signalName, container));
                }
                properties.addSorted(typeDescriptors);
            }
        }
        return properties;
    }

}
