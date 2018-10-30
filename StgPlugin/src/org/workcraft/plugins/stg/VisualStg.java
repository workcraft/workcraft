package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.plugins.stg.properties.SignalNamePropertyDescriptor;
import org.workcraft.plugins.stg.properties.SignalTypePropertyDescriptor;
import org.workcraft.plugins.stg.tools.*;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;

import java.awt.geom.Point2D;
import java.util.*;

@DisplayName("Signal Transition Graph")
public class VisualStg extends AbstractVisualModel {

    public VisualStg(Stg model) {
        this(model, null);
    }

    public VisualStg(Stg model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    @Override
    public void createDefaultStructure() {
        super.createDefaultStructure();
        // Convert dual producer-consumer arcs into read-arcs
        HashSet<Pair<VisualConnection, VisualConnection>> dualArcs = PetriNetUtils.getSelectedOrAllDualArcs(this);
        PetriNetUtils.convertDualArcsToReadArcs(this, dualArcs);

        // Hide implicit places
        // FIXME: Implicit places should not appear in the first place.
        for (VisualStgPlace vp: getVisualPlaces()) {
            Place place = vp.getReferencedPlace();
            if ((place instanceof StgPlace) && ((StgPlace) place).isImplicit()) {
                maybeMakeImplicit(vp, false);
            }
        }
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new StgSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new StgConnectionTool());
        tools.add(new ReadArcConnectionTool());
        tools.add(new StgPlaceGeneratorTool());
        tools.add(new StgSignalTransitionGeneratorTool());
        tools.add(new StgDummyTransitionGeneratorTool());
        tools.add(new StgSimulationTool());
        tools.add(new EncodingConflictAnalyserTool());
        setGraphEditorTools(tools);
    }

    public Stg getMathModel() {
        return (Stg) super.getMathModel();
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
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
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection)
            throws InvalidConnectionException {

        validateConnection(first, second);

        VisualConnection connection = null;
        if (first instanceof VisualNamedTransition) {
            if (second instanceof VisualNamedTransition) {
                connection = createImplicitPlaceConnection((VisualNamedTransition) first, (VisualNamedTransition) second);
            } else if (second instanceof VisualImplicitPlaceArc) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc) second;
                VisualStgPlace place = makeExplicit(con);
                connection = connect(first, place);
            } else if ((second instanceof VisualStgPlace) || (second instanceof VisualReplicaPlace)) {
                connection = createSimpleConnection((VisualNode) first, (VisualNode) second, mConnection);
            }
        } else if (first instanceof VisualImplicitPlaceArc) {
            if (second instanceof VisualNamedTransition) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc) first;
                VisualStgPlace place = makeExplicit(con);
                connection = connect(place, second);
            }
        } else if ((first instanceof VisualStgPlace) || (first instanceof VisualReplicaPlace)) {
            connection = createSimpleConnection((VisualNode) first, (VisualNode) second, mConnection);
        }
        return connection;
    }

    private VisualImplicitPlaceArc createImplicitPlaceConnection(VisualNamedTransition t1, VisualNamedTransition t2)
            throws InvalidConnectionException  {

        ImplicitPlaceConnection c = getMathModel().connect(t1.getReferencedTransition(), t2.getReferencedTransition());

        StgPlace implicitPlace = c.getImplicitPlace();
        MathConnection con1 = c.getFirst();
        MathConnection con2 = c.getSecond();

        VisualImplicitPlaceArc connection = new VisualImplicitPlaceArc(t1, t2, con1, con2, implicitPlace);
        Hierarchy.getNearestContainer(t1, t2).add(connection);
        return connection;
    }

    private VisualConnection createSimpleConnection(VisualNode first, VisualNode second, MathConnection mConnection)
            throws InvalidConnectionException {

        if (mConnection == null) {
            MathNode firstRef = getMathReference(first);
            MathNode secondRef = getMathReference(second);
            mConnection = getMathModel().connect(firstRef, secondRef);
        }
        VisualConnection connection = new VisualConnection(mConnection, first, second);
        Container container = Hierarchy.getNearestContainer(first, second);
        container.add(connection);
        return connection;
    }

    @Override
    public void validateUndirectedConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
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
    public VisualConnection connectUndirected(VisualNode first, VisualNode second) throws InvalidConnectionException {
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
            MathConnection mConsumingConnection = getMathModel().connect(mPlace, mTransition);
            MathConnection mProducingConnection = getMathModel().connect(mTransition, mPlace);

            connection = new VisualReadArc(place, transition, mConsumingConnection, mProducingConnection);
            Container container = Hierarchy.getNearestContainer(place, transition);
            container.add(connection);
        }
        return connection;
    }

    public VisualStgPlace makeExplicit(VisualImplicitPlaceArc connection) {
        Container group = Hierarchy.getNearestAncestor(connection, Container.class);
        Point2D splitPoint = connection.getSplitPoint();
        StgPlace implicitPlace = connection.getImplicitPlace();
        getMathModel().makeExplicit(implicitPlace);
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
        Collection<VisualNode> preset = getPreset(place);
        Collection<VisualNode> postset = getPostset(place);
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
        StgPlace mathPlace = getMathModel().createPlace(mathRef, null);
        return createVisualComponent(mathPlace, VisualStgPlace.class);
    }

    public VisualStgPlace createVisualPlace(String mathRef, Container container) {
        StgPlace mathPlace = getMathModel().createPlace(mathRef, null);
        return createVisualComponent(mathPlace, VisualStgPlace.class, container);
    }

    public VisualDummyTransition createVisualDummyTransition(String mathRef) {
        DummyTransition mathTransition = getMathModel().createDummyTransition(mathRef, null);
        return createVisualComponent(mathTransition, VisualDummyTransition.class);
    }

    public VisualDummyTransition createVisualDummyTransition(String mathRef, Container container) {
        DummyTransition mathTransition = getMathModel().createDummyTransition(mathRef, null);
        return createVisualComponent(mathTransition, VisualDummyTransition.class, container);
    }

    public VisualSignalTransition createVisualSignalTransition(String signalRef, Signal.Type type,
            SignalTransition.Direction direction) {

        String mathName = null;
        if ((signalRef != null) && (direction != null)) {
            mathName = signalRef + direction.toString();
        }
        SignalTransition mathTransition = getMathModel().createSignalTransition(mathName, null);
        mathTransition.setSignalType(type);
        return createVisualComponent(mathTransition, VisualSignalTransition.class);
    }

    public VisualSignalTransition createVisualSignalTransition(String signalRef, Signal.Type type,
            SignalTransition.Direction direction, Container container) {

        String mathName = null;
        if ((signalRef != null) && (direction != null)) {
            mathName = signalRef + direction.toString();
        }
        SignalTransition mathTransition = getMathModel().createSignalTransition(mathName, null);
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
            Stg stg = getMathModel();
            for (Signal.Type type : Signal.Type.values()) {
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
