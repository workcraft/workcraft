package org.workcraft.plugins.petri.utils;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.*;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class ConversionUtils {

    public static HashSet<Pair<VisualConnection, VisualConnection>> getSelectedOrAllDualArcs(final VisualModel visualModel) {
        HashSet<Pair<VisualConnection /* consuming arc */, VisualConnection /* producing arc */>> dualArcs = new HashSet<>();
        HashSet<VisualConnection> consumingArcs = ConversionUtils.getVisualConsumingArcs(visualModel);
        HashSet<VisualConnection> producingArcs = ConversionUtils.getVisualProducingArcs(visualModel);
        for (VisualConnection consumingArc: consumingArcs) {
            for (VisualConnection producingArc: producingArcs) {
                boolean isDualArcs = (consumingArc.getFirst() == producingArc.getSecond())
                        && (consumingArc.getSecond() == producingArc.getFirst());
                Collection<VisualNode> selection = visualModel.getSelection();
                boolean selectedArcsOrNoSelection = selection.isEmpty() || selection.contains(consumingArc) || selection.contains(producingArc);
                if (isDualArcs && selectedArcsOrNoSelection) {
                    dualArcs.add(new Pair<>(consumingArc, producingArc));
                }
            }
        }
        return dualArcs;
    }

    public static HashSet<VisualReadArc> convertDualArcsToReadArcs(final VisualModel visualModel,
            HashSet<Pair<VisualConnection, VisualConnection>> dualArcs) {

        HashSet<VisualReadArc> readArcs = new HashSet<>(dualArcs.size());
        for (Pair<VisualConnection, VisualConnection> dualArc: dualArcs) {
            VisualConnection consumingArc = dualArc.getFirst();
            VisualConnection producingArc = dualArc.getSecond();
            VisualReadArc readArc = ConversionUtils.convertDualArcToReadArc(visualModel, consumingArc, producingArc);
            if (readArc != null) {
                readArcs.add(readArc);
            }
        }
        return readArcs;
    }

    public static VisualReadArc convertDualArcToReadArc(VisualModel visualModel, VisualConnection consumingArc, VisualConnection producingArc) {
        VisualReadArc readArc = null;
        if (isVisualDualArcs(consumingArc, producingArc)) {
            VisualConnection connection = consumingArc;
            if (!isVisualConsumingArc(consumingArc)) {
                connection = producingArc;
            }
            VisualNode place = connection.getFirst();
            if (place instanceof VisualReplicaPlace) {
                place = copyReplicaPlace(visualModel, (VisualReplicaPlace) place);
            }
            VisualNode transition = connection.getSecond();

            boolean needsShapeInversion = !visualModel.getSelection().contains(connection);
            if (needsShapeInversion) {
                connection = producingArc;
            }

            visualModel.remove(consumingArc);
            visualModel.remove(producingArc);
            try {
                VisualConnection undirectedConnection = visualModel.connectUndirected(place, transition);
                if (undirectedConnection instanceof VisualReadArc) {
                    readArc = (VisualReadArc) undirectedConnection;
                    readArc.copyStyle(connection);
                    readArc.copyShape(connection);
                    if (needsShapeInversion) {
                        readArc.inverseShape();
                    }
                }
            } catch (InvalidConnectionException e) {
            }
        }
        return readArc;
    }

    public static VisualReadArc convertDirectedArcToReadArc(VisualModel visualModel, VisualConnection connection) {
        VisualReadArc readArc = null;
        if (isVisualProducingArc(connection)) {
            readArc = convertProducingArcToReadArc(visualModel, connection);
        } else if (isVisualConsumingArc(connection)) {
            readArc = convertConsumingArcToReadArc(visualModel, connection);
        }
        return readArc;
    }

    public static VisualReadArc convertProducingArcToReadArc(VisualModel visualModel, VisualConnection connection) {
        VisualReadArc readArc = null;
        VisualNode transition;
        VisualNode placeOrReplica;
        if (isVisualProducingArc(connection)) {
            transition = connection.getFirst();
            placeOrReplica = connection.getSecond();
            if (placeOrReplica instanceof VisualReplicaPlace) {
                placeOrReplica = copyReplicaPlace(visualModel, (VisualReplicaPlace) placeOrReplica);
            }
            // Remove producing and dual consuming arcs (including replicas)
            visualModel.remove(connection);
            VisualPlace place = (VisualPlace) ((placeOrReplica instanceof VisualPlace) ? placeOrReplica : ((VisualReplicaPlace) placeOrReplica).getMaster());
            VisualConnection dualConsumingArc = visualModel.getConnection(place, transition);
            if (dualConsumingArc != null) {
                visualModel.remove(dualConsumingArc);
            }
            for (Replica replica: place.getReplicas()) {
                VisualConnection dualReplicaConsumingArc = visualModel.getConnection((VisualReplicaPlace) replica, transition);
                if (dualReplicaConsumingArc != null) {
                    visualModel.remove(dualReplicaConsumingArc);
                }
            }
            // Create read-arc
            try {
                VisualConnection undirectedConnection = visualModel.connectUndirected(placeOrReplica, transition);
                if (undirectedConnection instanceof VisualReadArc) {
                    readArc = (VisualReadArc) undirectedConnection;
                    readArc.copyStyle(connection);
                    readArc.copyShape(connection);
                    readArc.inverseShape();
                }
            } catch (InvalidConnectionException e) {
            }
        }
        return readArc;
    }

    public static VisualReadArc convertConsumingArcToReadArc(VisualModel visualModel, VisualConnection connection) {
        VisualReadArc readArc = null;
        VisualNode placeOrReplica;
        VisualNode transition;
        if (isVisualConsumingArc(connection)) {
            placeOrReplica = connection.getFirst();
            if (placeOrReplica instanceof VisualReplicaPlace) {
                placeOrReplica = copyReplicaPlace(visualModel, (VisualReplicaPlace) placeOrReplica);
            }
            transition = connection.getSecond();
            // Remove consuming and dual producing arcs (including replicas)
            visualModel.remove(connection);
            VisualPlace place = (VisualPlace) ((placeOrReplica instanceof VisualPlace) ? placeOrReplica : ((VisualReplicaPlace) placeOrReplica).getMaster());
            VisualConnection dualProducingArc = visualModel.getConnection(transition, place);
            if (dualProducingArc != null) {
                visualModel.remove(dualProducingArc);
            }
            for (Replica replica: place.getReplicas()) {
                VisualConnection dualReplicaProducingArc = visualModel.getConnection(transition, (VisualReplicaPlace) replica);
                if (dualReplicaProducingArc != null) {
                    visualModel.remove(dualReplicaProducingArc);
                }
            }
            // Create read-arc
            try {
                VisualConnection undirectedConnection = visualModel.connectUndirected(placeOrReplica, transition);
                if (undirectedConnection instanceof VisualReadArc) {
                    readArc = (VisualReadArc) undirectedConnection;
                    readArc.copyStyle(connection);
                    readArc.copyShape(connection);
                }
            } catch (InvalidConnectionException e) {
            }
        }
        return readArc;
    }

    public static Pair<VisualConnection, VisualConnection> convertReadArcTotDualArc(VisualModel visualModel, VisualReadArc readArc) {
        VisualNode first = readArc.getFirst();
        VisualNode second = readArc.getSecond();
        VisualConnection consumingArc = null;
        VisualConnection producingArc = null;
        if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace)) && (second instanceof VisualTransition)) {
            try {
                if (first instanceof VisualReplicaPlace) {
                    first = copyReplicaPlace(visualModel, (VisualReplicaPlace) first);
                }
                visualModel.remove(readArc);

                consumingArc = visualModel.connect(first, second);
                consumingArc.copyStyle(readArc);
                ConnectionUtils.setDefaultStyle(consumingArc);
                consumingArc.copyShape(readArc);

                producingArc = visualModel.connect(second, first);
                producingArc.copyStyle(readArc);
                ConnectionUtils.setDefaultStyle(producingArc);
                producingArc.copyShape(readArc);
                producingArc.inverseShape();
            } catch (InvalidConnectionException e) {
            }
        }
        return new Pair<>(consumingArc, producingArc);
    }

    private static VisualReplicaPlace copyReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
        Container container = Hierarchy.getNearestContainer(replica);
        VisualComponent master = replica.getMaster();
        VisualReplicaPlace result = visualModel.createVisualReplica(master, VisualReplicaPlace.class, container);
        result.copyPosition(replica);
        result.copyStyle(replica);
        return result;
    }

    public static VisualConnection collapseReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
        VisualConnection result = null;
        HashSet<Connection> connections = new HashSet<>(visualModel.getConnections(replica));
        for (Connection c: connections) {
            if (c instanceof VisualConnection) {
                VisualConnection vc = (VisualConnection) c;
                VisualNode first = vc.getFirst();
                VisualNode second = vc.getSecond();
                if (replica == first) {
                    first = replica.getMaster();
                }
                if (replica == second) {
                    second = replica.getMaster();
                }
                Point2D replicaPositionInRootSpace = replica.getRootSpacePosition();
                LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(vc, replicaPositionInRootSpace);
                locationsInRootSpace.addFirst(replicaPositionInRootSpace);
                visualModel.remove(vc);
                try {
                    if (vc instanceof VisualReadArc) {
                        result = visualModel.connectUndirected(first, second);
                    } else {
                        result = visualModel.connect(first, second);
                    }
                    ConnectionHelper.addControlPoints(result, locationsInRootSpace);
                } catch (InvalidConnectionException e) {
                }
            }
        }
        return result;
    }

    public static VisualConnection replicateConnectedPlace(VisualModel visualModel, VisualConnection connection) {
        VisualConnection result = null;
        VisualNode first = connection.getFirst();
        VisualNode second = connection.getSecond();
        VisualPlace place = null;
        VisualTransition transition = null;
        boolean reverse = false;
        if ((first instanceof VisualPlace) && (second instanceof VisualTransition)) {
            place = (VisualPlace) first;
            transition = (VisualTransition) second;
            reverse = false;
        } else if ((second instanceof VisualPlace) && (first instanceof VisualTransition)) {
            place = (VisualPlace) second;
            transition = (VisualTransition) first;
            reverse = true;
        }
        if ((place != null) && (transition != null)) {
            Container container = Hierarchy.getNearestContainer(transition);
            VisualReplicaPlace replica = visualModel.createVisualReplica(place, VisualReplicaPlace.class, container);
            Point2D splitPointInRootSpace = getReplicaPositionInRootSpace(connection);
            replica.setRootSpacePosition(splitPointInRootSpace);
            LinkedList<Point2D> locationsInRootSpace;
            if (reverse) {
                locationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPointInRootSpace);
            } else {
                locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPointInRootSpace);
            }
            visualModel.remove(connection);
            try {
                if (connection instanceof VisualReadArc) {
                    result = visualModel.connectUndirected(replica, transition);
                } else {
                    if (reverse) {
                        result = visualModel.connect(transition, replica);
                    } else {
                        result = visualModel.connect(replica, transition);
                    }
                }
                ConnectionHelper.addControlPoints(result, locationsInRootSpace);
            } catch (InvalidConnectionException e) {
            }
        }
        return result;
    }

    private static Point2D getReplicaPositionInRootSpace(VisualConnection connection) {
        Point2D positionInRootSpace = null;
        Point2D positionInLocalSpace = null;
        ConnectionGraphic graphic = connection.getGraphic();
        if (graphic instanceof Polyline) {
            Polyline polyline = (Polyline) graphic;
            ControlPoint cp = null;
            if (connection.getFirst() instanceof VisualPlace) {
                cp = polyline.getFirstControlPoint();
            } else if (connection.getSecond() instanceof VisualPlace) {
                cp = polyline.getLastControlPoint();
            }
            if (cp != null) {
                positionInLocalSpace = cp.getPosition();
            }
        }
        if (positionInLocalSpace == null) {
            positionInLocalSpace = connection.getSplitPoint();
        }
        if (positionInLocalSpace != null) {
            AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
            positionInRootSpace = localToRootTransform.transform(positionInLocalSpace, null);
        }
        return positionInRootSpace;
    }

    public static boolean hasReadArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualPlace place = null;
        VisualTransition transition = null;
        if (first instanceof VisualPlace) {
            place = (VisualPlace) first;
        } else if (first instanceof VisualReplicaPlace) {
            VisualReplicaPlace r = (VisualReplicaPlace) first;
            place = (VisualPlace) r.getMaster();
        }
        if (second instanceof VisualTransition) {
            transition = (VisualTransition) second;
        }
        if ((place != null) && (transition != null)) {
            for (Replica replica: place.getReplicas()) {
                if (replica instanceof VisualReplicaPlace) {
                    VisualReplicaPlace replicaPlace = (VisualReplicaPlace) replica;
                    VisualConnection connection = visualModel.getConnection(replicaPlace, transition);
                    found = connection instanceof VisualReadArc;
                }
            }
            if (!found) {
                VisualConnection connection = visualModel.getConnection(place, transition);
                found = connection instanceof VisualReadArc;
            }
        }
        return found;
    }

    public static boolean hasProducingArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualPlace place = null;
        VisualTransition transition = null;
        if (first instanceof VisualTransition) {
            transition = (VisualTransition) first;
        }
        if (second instanceof VisualPlace) {
            place = (VisualPlace) second;
        } else if (second instanceof VisualReplicaPlace) {
            VisualReplicaPlace r = (VisualReplicaPlace) second;
            place = (VisualPlace) r.getMaster();
        }
        if ((transition != null) && (place != null)) {
            for (Replica replica: place.getReplicas()) {
                if (replica instanceof VisualReplicaPlace) {
                    VisualReplicaPlace replicaPlace = (VisualReplicaPlace) replica;
                    VisualConnection connection = visualModel.getConnection(transition, replicaPlace);
                    found = (connection != null) && !(connection instanceof VisualReadArc);
                }
            }
            if (!found) {
                VisualConnection connection = visualModel.getConnection(transition, place);
                found = (connection != null) && !(connection instanceof VisualReadArc);
            }
        }
        return found;
    }

    public static boolean hasConsumingArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualPlace place = null;
        VisualTransition transition = null;
        if (first instanceof VisualPlace) {
            place = (VisualPlace) first;
        } else if (first instanceof VisualReplicaPlace) {
            VisualReplicaPlace r = (VisualReplicaPlace) first;
            place = (VisualPlace) r.getMaster();
        }
        if (second instanceof VisualTransition) {
            transition = (VisualTransition) second;
        }
        if ((place != null) && (transition != null)) {
            for (Replica replica: place.getReplicas()) {
                if (replica instanceof VisualReplicaPlace) {
                    VisualReplicaPlace replicaPlace = (VisualReplicaPlace) replica;
                    VisualConnection connection = visualModel.getConnection(replicaPlace, transition);
                    found = (connection != null) && !(connection instanceof VisualReadArc);
                }
            }
            if (!found) {
                VisualConnection connection = visualModel.getConnection(place, transition);
                found = (connection != null) && !(connection instanceof VisualReadArc);
            }
        }
        return found;
    }

    public static boolean hasImplicitPlaceArcConnection(VisualModel visualModel, VisualNode first, VisualNode second) {
        boolean found = false;
        VisualTransition predTransition = null;
        VisualTransition succTransition = null;
        if (first instanceof VisualTransition) {
            predTransition = (VisualTransition) first;
        }
        if (second instanceof VisualTransition) {
            succTransition = (VisualTransition) second;
        }
        if ((predTransition != null) && (succTransition != null)) {
            VisualConnection connection = visualModel.getConnection(predTransition, succTransition);
            found = connection != null;
        }
        return found;
    }

    public static HashSet<VisualConnection> getVisualConsumingArcs(VisualModel visualModel) {
        HashSet<VisualConnection> connections = new HashSet<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
            if (isVisualConsumingArc(connection)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    public static HashSet<VisualConnection> getVisualProducingArcs(VisualModel visualModel) {
        HashSet<VisualConnection> connections = new HashSet<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
            if (isVisualProducingArc(connection)) {
                connections.add(connection);
            }
        }
        return connections;
    }

    public static HashSet<VisualReadArc> getVisualReadArcs(VisualModel visualModel) {
        return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualReadArc.class));
    }

    public static HashSet<VisualReplicaPlace> getVisualReplicaPlaces(VisualModel visualModel) {
        return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualReplicaPlace.class));
    }

    public static HashSet<VisualPlace> getVisualPlaces(VisualModel visualModel) {
        return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualPlace.class));
    }

    public static HashSet<VisualTransition> getVisualTransitions(VisualModel visualModel) {
        return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualTransition.class));
    }

    public static boolean isVisualTransition(VisualNode node) {
        return node instanceof VisualTransition;
    }

    public static boolean isVisualPlaceOrReplicaPlace(VisualNode node) {
        return (node instanceof VisualPlace) || (node instanceof VisualReplicaPlace);
    }

    public static boolean isVisualProducingArc(VisualNode node) {
        if ((node instanceof VisualConnection) && !(node instanceof VisualReadArc)) {
            VisualConnection connection = (VisualConnection) node;
            return isVisualTransition(connection.getFirst()) && isVisualPlaceOrReplicaPlace(connection.getSecond());
        }
        return false;
    }

    public static boolean isVisualConsumingArc(VisualNode node) {
        if ((node instanceof VisualConnection) && !(node instanceof VisualReadArc)) {
            VisualConnection connection = (VisualConnection) node;
            return isVisualPlaceOrReplicaPlace(connection.getFirst()) && isVisualTransition(connection.getSecond());
        }
        return false;
    }

    public static boolean isVisualDualArcs(VisualNode node1, VisualNode node2) {
        VisualConnection consumingArc = null;
        VisualConnection producingArc = null;
        if (isVisualConsumingArc(node1)) {
            consumingArc = (VisualConnection) node1;
        } else if (isVisualProducingArc(node1)) {
            producingArc = (VisualConnection) node1;
        }
        if (isVisualConsumingArc(node2)) {
            consumingArc = (VisualConnection) node2;
        } else if (isVisualProducingArc(node2)) {
            producingArc = (VisualConnection) node2;
        }
        if ((consumingArc != null) && (producingArc != null)) {
            VisualNode place1 = consumingArc.getFirst();
            if (place1 instanceof VisualReplicaPlace) {
                place1 = ((VisualReplicaPlace) place1).getMaster();
            }
            VisualNode transition1 = consumingArc.getSecond();

            VisualNode transition2 = producingArc.getFirst();
            VisualNode place2 = producingArc.getSecond();
            if (place2 instanceof VisualReplicaPlace) {
                place2 = ((VisualReplicaPlace) place2).getMaster();
            }

            return (place1 == place2) && (transition1 == transition2);
        }
        return false;
    }

    public static HashSet<Place> getIsolatedMarkedPlaces(PetriModel model) {
        HashSet<Place> result = new HashSet<>();
        for (Place place: model.getPlaces()) {
            if ((place.getTokens() > 0) && model.getConnections(place).isEmpty()) {
                result.add(place);
            }
        }
        return result;
    }

    public static void removeIsolatedMarkedPlaces(VisualModel visualModel) {
        MathModel model = visualModel.getMathModel();
        for (VisualPlace visualPlace: getVisualPlaces(visualModel)) {
            Place place = visualPlace.getReferencedComponent();
            if ((place.getTokens() > 0) && model.getConnections(place).isEmpty()) {
                visualModel.remove(visualPlace);
            }
        }
    }

}
