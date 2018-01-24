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
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
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
@CustomTools(StgToolsProvider.class)
public class VisualStg extends AbstractVisualModel {

    public VisualStg() {
        this(new Stg(), null);
    }

    public VisualStg(Stg model) {
        this(model, null);
    }

    public VisualStg(Stg model, VisualGroup root) {
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
        for (VisualStgPlace vp: getVisualPlaces()) {
            Place p = vp.getReferencedPlace();
            if (p instanceof StgPlace) {
                StgPlace pp = (StgPlace) p;
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
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
        if (((first instanceof VisualStgPlace) || (first instanceof VisualReplicaPlace) || (first instanceof VisualImplicitPlaceArc))
                && ((second instanceof VisualStgPlace) || (second instanceof VisualReplicaPlace) || (second instanceof VisualImplicitPlaceArc))) {
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
        if (PetriNetUtils.hasImplicitPlaceArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This implicit place arc already exists.");
        }
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualConnection connection = null;
        if (first instanceof VisualTransition) {
            if (second instanceof VisualTransition) {
                connection = createImplicitPlaceConnection((VisualTransition) first, (VisualTransition) second);
            } else if (second instanceof VisualImplicitPlaceArc) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc) second;
                VisualStgPlace place = makeExplicit(con);
                connection = connect(first, place);
            } else if ((second instanceof VisualStgPlace) || (second instanceof VisualReplicaPlace)) {
                connection = createSimpleConnection((VisualNode) first, (VisualNode) second, mConnection);
            }
        } else if (first instanceof VisualImplicitPlaceArc) {
            if (second instanceof VisualTransition) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc) first;
                VisualStgPlace place = makeExplicit(con);
                connection = connect(place, second);
            }
        } else if ((first instanceof VisualStgPlace) || (first instanceof VisualReplicaPlace)) {
            connection = createSimpleConnection((VisualNode) first, (VisualNode) second, mConnection);
        }
        return connection;
    }

    private VisualImplicitPlaceArc createImplicitPlaceConnection(VisualTransition t1, VisualTransition t2) throws InvalidConnectionException {
        Stg stg = (Stg) getMathModel();
        final ConnectionResult connectResult = stg.connect(t1.getReferencedTransition(), t2.getReferencedTransition());

        StgPlace implicitPlace = connectResult.getImplicitPlace();
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

        Stg stg = (Stg) getMathModel();
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
    public void validateUndirectedConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }
        if (((first instanceof VisualStgPlace) || (first instanceof VisualReplicaPlace))
                && ((second instanceof VisualStgPlace) || (second instanceof VisualReplicaPlace))) {
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
            place = (VisualNode) second;
            transition = (VisualNode) first;
        } else if (second instanceof VisualTransition) {
            place = (VisualNode) first;
            transition = (VisualNode) second;
        }
        VisualConnection connection = null;
        if ((place != null) && (transition != null)) {
            connection = createReadArcConnection(place, transition);
        }
        return connection;
    }

    private VisualReadArc createReadArcConnection(VisualNode place, VisualNode transition)
             throws InvalidConnectionException {
        Stg stg = (Stg) getMathModel();

        Place mPlace = null;
        if (place instanceof VisualStgPlace) {
            mPlace = ((VisualStgPlace) place).getReferencedPlace();
        } else if (place instanceof VisualReplicaPlace) {
            mPlace = ((VisualReplicaPlace) place).getReferencedPlace();
        }
        Transition mTransition = null;
        if (transition instanceof VisualTransition) {
            mTransition = ((VisualTransition) transition).getReferencedTransition();
        }

        VisualReadArc connection = null;
        if ((mPlace != null) && (mTransition != null)) {
            MathConnection mConsumingConnection = stg.connect(mPlace, mTransition).getSimpleResult();
            MathConnection mProducingConnection = stg.connect(mTransition, mPlace).getSimpleResult();

            connection = new VisualReadArc(place, transition, mConsumingConnection, mProducingConnection);
            Hierarchy.getNearestContainer(place, transition).add(connection);
        }
        return connection;
    }

    public VisualStgPlace makeExplicit(VisualImplicitPlaceArc connection) {
        Container group = Hierarchy.getNearestAncestor(connection, Container.class);
        Stg stg = (Stg) getMathModel();
        Point2D splitPoint = connection.getSplitPoint();
        StgPlace implicitPlace = connection.getImplicitPlace();
        stg.makeExplicit(implicitPlace);
        VisualStgPlace place = new VisualStgPlace(implicitPlace);
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

    public VisualImplicitPlaceArc maybeMakeImplicit(VisualStgPlace place, boolean preserveConnectionShape) {
        VisualImplicitPlaceArc connection = null;
        Collection<Node> preset = getPreset(place);
        Collection<Node> postset = getPostset(place);
        Collection<Replica> replicas = place.getReplicas();
        if ((preset.size() == 1) && (postset.size() == 1) && replicas.isEmpty()) {
            VisualComponent first = (VisualComponent) preset.iterator().next();
            VisualComponent second = (VisualComponent) postset.iterator().next();
            if (!PetriNetUtils.hasImplicitPlaceArcConnection(this, first, second)) {
                final StgPlace stgPlace = (StgPlace) place.getReferencedPlace();
                stgPlace.setImplicit(true);

                VisualConnection con1 = null;
                VisualConnection con2 = null;
                Collection<Connection> connections = new ArrayList<>(getConnections(place));
                for (Connection con: connections) {
                    if (con.getFirst() == place) {
                        con2 = (VisualConnection) con;
                    } else if (con.getSecond() == place) {
                        con1 = (VisualConnection) con;
                    }
                }
                MathConnection refCon1 = con1.getReferencedConnection();
                MathConnection refCon2 = con2.getReferencedConnection();
                connection = new VisualImplicitPlaceArc(first, second, refCon1, refCon2, (StgPlace) place.getReferencedPlace());
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
        return connection;
    }

    public VisualStgPlace createVisualPlace(String mathRef) {
        Stg stg = (Stg) getMathModel();
        StgPlace mathPlace = stg.createPlace(mathRef, null);
        return createVisualComponent(mathPlace, VisualStgPlace.class);
    }

    public VisualStgPlace createVisualPlace(String mathRef, Container container) {
        Stg stg = (Stg) getMathModel();
        StgPlace mathPlace = stg.createPlace(mathRef, null);
        return createVisualComponent(mathPlace, VisualStgPlace.class, container);
    }

    public VisualDummyTransition createVisualDummyTransition(String mathRef) {
        Stg stg = (Stg) getMathModel();
        DummyTransition mathTransition = stg.createDummyTransition(mathRef, null);
        return createVisualComponent(mathTransition, VisualDummyTransition.class);
    }

    public VisualDummyTransition createVisualDummyTransition(String mathRef, Container container) {
        Stg stg = (Stg) getMathModel();
        DummyTransition mathTransition = stg.createDummyTransition(mathRef, null);
        return createVisualComponent(mathTransition, VisualDummyTransition.class, container);
    }

    public VisualSignalTransition createVisualSignalTransition(String signalRef, SignalTransition.Type type,
            Direction direction) {
        Stg stg = (Stg) getMathModel();
        String mathName = null;
        if ((signalRef != null) && (direction != null)) {
            mathName = signalRef + direction.toString();
        }
        SignalTransition mathTransition = stg.createSignalTransition(mathName, null);
        mathTransition.setSignalType(type);
        return createVisualComponent(mathTransition, VisualSignalTransition.class);
    }

    public VisualSignalTransition createVisualSignalTransition(String signalRef, SignalTransition.Type type,
            Direction direction, Container container) {
        Stg stg = (Stg) getMathModel();
        String mathName = null;
        if ((signalRef != null) && (direction != null)) {
            mathName = signalRef + direction.toString();
        }
        SignalTransition mathTransition = stg.createSignalTransition(mathName, null);
        mathTransition.setSignalType(type);
        return createVisualComponent(mathTransition, VisualSignalTransition.class, container);
    }

    public Collection<VisualStgPlace> getVisualPlaces() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualStgPlace.class);
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

    public VisualStgPlace getVisualPlace(StgPlace place) {
        for (VisualStgPlace vp: getVisualPlaces()) {
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
            Stg stg = (Stg) getMathModel();
            for (Type type : Type.values()) {
                Container container = NamespaceHelper.getMathContainer(this, getCurrentLevel());
                for (final String signalName : stg.getSignalNames(type, container)) {
                    if (stg.getSignalTransitions(signalName, container).isEmpty()) continue;
                    SignalNamePropertyDescriptor symbolDescriptor = new SignalNamePropertyDescriptor(stg, signalName, container);
                    properties.insertOrderedByFirstWord(symbolDescriptor);
                    SignalTypePropertyDescriptor typeDescriptor = new SignalTypePropertyDescriptor(stg, signalName, container);
                    properties.insertOrderedByFirstWord(typeDescriptor);
                }
            }
        }
        return properties;
    }

    public String getSignalReference(VisualSignalTransition transition) {
        String ref = getNodeMathReference(transition);
        String signalName = transition.getSignalName();
        String signalPath = NamespaceHelper.getParentReference(ref);
        return NamespaceHelper.getReference(signalPath, signalName);
    }

    @Override
    public String getNodeMathReference(Node node) {
        if (node instanceof VisualImplicitPlaceArc) {
            VisualImplicitPlaceArc connection = (VisualImplicitPlaceArc) node;
            node = connection.getImplicitPlace();
        }
        return super.getNodeMathReference(node);
    }

}
