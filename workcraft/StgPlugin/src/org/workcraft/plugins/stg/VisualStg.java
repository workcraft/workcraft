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
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.petri.tools.ReadArcConnectionTool;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.plugins.stg.tools.*;
import org.workcraft.plugins.stg.utils.ConnectionUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

@DisplayName("Signal Transition Graph")
public class VisualStg extends AbstractVisualModel {

    public VisualStg(Stg model) {
        this(model, null);
    }

    public VisualStg(Stg model, VisualGroup root) {
        super(model, root);
    }

    @Override
    public void createDefaultStructure() {
        super.createDefaultStructure();
        // Convert dual producer-consumer arcs into read-arcs
        HashSet<Pair<VisualConnection, VisualConnection>> dualArcs = ConversionUtils.getSelectedOrAllDualArcs(this);
        ConversionUtils.convertDualArcsToReadArcs(this, dualArcs);

        // Hide implicit places
        // FIXME: Implicit places should not appear in the first place.
        for (VisualStgPlace vp: getVisualPlaces()) {
            StgPlace place = vp.getReferencedComponent();
            if ((place != null) && place.isImplicit()) {
                maybeMakeImplicit(vp, false);
            }
        }
    }

    @Override
    public void registerGraphEditorTools() {
        addGraphEditorTool(new StgSelectionTool());
        addGraphEditorTool(new CommentGeneratorTool());
        addGraphEditorTool(new StgConnectionTool());
        addGraphEditorTool(new ReadArcConnectionTool());
        addGraphEditorTool(new PlaceGeneratorTool());
        addGraphEditorTool(new SignalTransitionGeneratorTool());
        addGraphEditorTool(new DummyTransitionGeneratorTool());
        addGraphEditorTool(new StgSimulationTool());
        addGraphEditorTool(new EncodingConflictAnalyserTool());
    }

    @Override
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
        if (ConnectionUtils.hasReadArcConnection(this, first, second) || ConnectionUtils.hasReadArcConnection(this, second, first)) {
            throw new InvalidConnectionException("Nodes are already connected by a read-arc.");
        }
        if (ConnectionUtils.hasProducingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This producing arc already exists.");
        }
        if (ConnectionUtils.hasConsumingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This consuming arc already exists.");
        }
        if (ConnectionUtils.hasImplicitPlaceArcConnection(this, first, second)) {
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
                connection = createSimpleConnection(first, second, mConnection);
            }
        } else if (first instanceof VisualImplicitPlaceArc) {
            if (second instanceof VisualNamedTransition) {
                VisualImplicitPlaceArc con = (VisualImplicitPlaceArc) first;
                VisualStgPlace place = makeExplicit(con);
                connection = connect(place, second);
            }
        } else if ((first instanceof VisualStgPlace) || (first instanceof VisualReplicaPlace)) {
            connection = createSimpleConnection(first, second, mConnection);
        }
        return connection;
    }

    private VisualImplicitPlaceArc createImplicitPlaceConnection(VisualNamedTransition t1, VisualNamedTransition t2)
            throws InvalidConnectionException  {

        ImplicitPlaceConnection c = getMathModel().connect(t1.getReferencedComponent(), t2.getReferencedComponent());

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
            MathNode firstRef = getReferencedComponent(first);
            MathNode secondRef = getReferencedComponent(second);
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
        if (ConnectionUtils.hasReadArcConnection(this, first, second)
                || ConnectionUtils.hasReadArcConnection(this, second, first)
                || ConnectionUtils.hasProducingArcConnection(this, first, second)
                || ConnectionUtils.hasProducingArcConnection(this, second, first)
                || ConnectionUtils.hasConsumingArcConnection(this, first, second)
                || ConnectionUtils.hasConsumingArcConnection(this, second, first)) {
            throw new InvalidConnectionException("Nodes are already connected.");
        }
    }

    @Override
    public VisualConnection connectUndirected(VisualNode first, VisualNode second) throws InvalidConnectionException {
        validateUndirectedConnection(first, second);

        VisualNode place = null;
        VisualNode transition = null;
        if (first instanceof VisualTransition) {
            place = second;
            transition = first;
        } else if (second instanceof VisualTransition) {
            place = first;
            transition = second;
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
            mPlace = ((VisualStgPlace) place).getReferencedComponent();
        } else if (place instanceof VisualReplicaPlace) {
            mPlace = ((VisualReplicaPlace) place).getReferencedPlace();
        }
        Transition mTransition = null;
        if (transition instanceof VisualTransition) {
            mTransition = ((VisualTransition) transition).getReferencedComponent();
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
            if (!ConnectionUtils.hasImplicitPlaceArcConnection(this, first, second)) {
                final StgPlace stgPlace = (StgPlace) place.getReferencedComponent();
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
                connection = new VisualImplicitPlaceArc(first, second, refCon1, refCon2, (StgPlace) place.getReferencedComponent());
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

    public VisualStgPlace createVisualPlace(String mathName) {
        return createVisualPlace(mathName, getRoot());
    }

    public VisualStgPlace createVisualPlace(String mathName, Container container) {
        if (NamespaceHelper.isHierarchical(mathName)) {
            throw new RuntimeException("Cannot create a place with the hierarchical name '" + mathName + "'.");
        }
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        StgPlace mathPlace = getMathModel().createPlace(mathName, mathContainer);
        return createVisualComponent(mathPlace, VisualStgPlace.class, container);
    }

    public VisualDummyTransition createVisualDummyTransition(String mathName) {
        return createVisualDummyTransition(mathName, getRoot());
    }

    public VisualDummyTransition createVisualDummyTransition(String mathName, Container container) {
        if (NamespaceHelper.isHierarchical(mathName)) {
            throw new RuntimeException("Cannot create a dummy with the hierarchical name '" + mathName + "'.");
        }
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        DummyTransition mathTransition = getMathModel().createDummyTransition(mathName, mathContainer);
        return createVisualComponent(mathTransition, VisualDummyTransition.class, container);
    }

    public VisualSignalTransition createVisualSignalTransition(String signalName, Signal.Type type,
            SignalTransition.Direction direction) {

        return createVisualSignalTransition(signalName, type, direction, getRoot());
    }

    public VisualSignalTransition createVisualSignalTransition(String signalName, Signal.Type type,
            SignalTransition.Direction direction, Container container) {

        if (NamespaceHelper.isHierarchical(signalName)) {
            throw new RuntimeException("Cannot create a transition of a signal with the hierarchical name '" + signalName + "'.");
        }

        String transitionName = (signalName == null) || (direction == null) ? null : signalName + direction.toString();
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        SignalTransition mathTransition = getMathModel().createSignalTransition(transitionName, mathContainer);
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

    public Collection<VisualImplicitPlaceArc> getVisualImplicitPlaceArcs() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualImplicitPlaceArc.class);
    }

    public String getSignalReference(VisualSignalTransition transition) {
        String ref = getMathReference(transition);
        String signalName = transition.getSignalName();
        String signalPath = NamespaceHelper.getParentReference(ref);
        return NamespaceHelper.getReference(signalPath, signalName);
    }

    @Override
    public String getMathReference(Node node) {
        if (node instanceof VisualImplicitPlaceArc) {
            VisualImplicitPlaceArc connection = (VisualImplicitPlaceArc) node;
            node = connection.getImplicitPlace();
        }
        return super.getMathReference(node);
    }

    @Override
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        Stg stg = getMathModel();
        if (node == null) {
            properties.add(StgPropertyHelper.getRefinementProperty(stg));
            properties.add(PropertyHelper.getSignalSectionProperty(this));
            properties.addAll(StgPropertyHelper.getSignalProperties(this));
        } else if (node instanceof VisualSignalTransition) {
            SignalTransition transition = ((VisualSignalTransition) node).getReferencedComponent();
            properties.removeByName(AbstractVisualModel.PROPERTY_NAME);
            properties.add(StgPropertyHelper.getSignalNameProperty(stg, transition));
            properties.add(StgPropertyHelper.getSignalTypeProperty(stg, transition));
            properties.add(StgPropertyHelper.getDirectionProperty(stg, transition));
            if (StgSettings.getShowTransitionInstance()) {
                properties.add(StgPropertyHelper.getInstanceProperty(stg, transition));
            }
        } else if (node instanceof VisualDummyTransition) {
            DummyTransition dummy = ((VisualDummyTransition) node).getReferencedComponent();
            if (StgSettings.getShowTransitionInstance()) {
                properties.add(StgPropertyHelper.getInstanceProperty(stg, dummy));
            }
        }
        return properties;
    }

}
