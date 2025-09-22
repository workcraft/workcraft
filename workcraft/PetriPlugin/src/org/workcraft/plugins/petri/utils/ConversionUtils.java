package org.workcraft.plugins.petri.utils;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.*;

public class ConversionUtils {

    public static HashSet<Pair<VisualConnection, VisualConnection>> getSelectedOrAllDualArcs(
            VisualModel visualModel, Collection<VisualNode> selectionOrNull) {

        HashSet<Pair<VisualConnection /* consuming arc */, VisualConnection /* producing arc */>> dualArcs = new HashSet<>();
        Map<VisualTransition, Map<VisualNode, VisualConnection>> transitionToPredMap = new HashMap<>();
        Map<VisualTransition, Map<VisualNode, VisualConnection>> transitionToSuccMap = new HashMap<>();
        for (VisualConnection connection : Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
            if (connection instanceof VisualReadArc) {
                continue;
            }
            VisualNode fromNode = connection.getFirst();
            VisualNode toNode = connection.getSecond();
            if (!(fromNode instanceof VisualTransition) && (toNode instanceof VisualTransition toTransition)) {
                transitionToPredMap.computeIfAbsent(toTransition, k -> new HashMap<>())
                        .put(fromNode, connection);
            }
            if ((fromNode instanceof VisualTransition fromTransition) && !(toNode instanceof VisualTransition)) {
                transitionToSuccMap.computeIfAbsent(fromTransition, k -> new HashMap<>())
                        .put(toNode, connection);
            }
        }
        for (VisualTransition transition : Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualTransition.class)) {
            Map<VisualNode, VisualConnection> predMap = transitionToPredMap.getOrDefault(transition, Collections.emptyMap());
            Map<VisualNode, VisualConnection> succMap = transitionToSuccMap.getOrDefault(transition, Collections.emptyMap());
            Set<VisualNode> overlapNodes = new HashSet<>(predMap.keySet());
            overlapNodes.retainAll(succMap.keySet());
            for (VisualNode overlapNode : overlapNodes) {
                VisualConnection consumingArc = predMap.get(overlapNode);
                VisualConnection producingArc = succMap.get(overlapNode);
                if ((selectionOrNull == null)
                        || selectionOrNull.contains(consumingArc)
                        || selectionOrNull.contains(producingArc)) {

                    dualArcs.add(new Pair<>(consumingArc, producingArc));
                }
            }
        }
        return dualArcs;
    }

    public static Set<VisualReadArc> convertDualArcsToReadArcs(VisualModel visualModel) {
        return convertDualArcsToReadArcs(visualModel, getSelectedOrAllDualArcs(visualModel, null));
    }

    public static Set<VisualReadArc> convertDualArcsToReadArcs(VisualModel visualModel,
            Set<Pair<VisualConnection, VisualConnection>> dualArcs) {

        Set<VisualReadArc> readArcs = new HashSet<>(dualArcs.size());
        for (Pair<VisualConnection, VisualConnection> dualArc : dualArcs) {
            VisualConnection consumingArc = dualArc.getFirst();
            VisualConnection producingArc = dualArc.getSecond();
            VisualReadArc readArc = ConversionUtils.convertDualArcToReadArc(visualModel, consumingArc, producingArc);
            if (readArc != null) {
                readArcs.add(readArc);
            }
        }
        return readArcs;
    }

    public static VisualReadArc convertDualArcToReadArc(VisualModel visualModel, VisualConnection consumingArc,
            VisualConnection producingArc) {

        VisualReadArc readArc = null;
        if (areVisualDualArcs(consumingArc, producingArc)) {
            VisualConnection connection = consumingArc;
            if (!ConnectionUtils.isVisualConsumingArc(consumingArc)) {
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
            } catch (InvalidConnectionException ignored) {
            }
        }
        return readArc;
    }

    public static VisualReadArc convertDirectedArcToReadArc(VisualModel visualModel, VisualConnection connection) {
        VisualReadArc readArc = null;
        if (ConnectionUtils.isVisualProducingArc(connection)) {
            readArc = convertProducingArcToReadArc(visualModel, connection);
        } else if (ConnectionUtils.isVisualConsumingArc(connection)) {
            readArc = convertConsumingArcToReadArc(visualModel, connection);
        }
        return readArc;
    }

    public static VisualReadArc convertProducingArcToReadArc(VisualModel visualModel, VisualConnection connection) {
        VisualReadArc readArc = null;
        VisualNode transition;
        VisualNode placeOrReplica;
        if (ConnectionUtils.isVisualProducingArc(connection)) {
            transition = connection.getFirst();
            placeOrReplica = connection.getSecond();
            if (placeOrReplica instanceof VisualReplicaPlace) {
                placeOrReplica = copyReplicaPlace(visualModel, (VisualReplicaPlace) placeOrReplica);
            }
            // Remove producing and dual consuming arcs (including replicas)
            visualModel.remove(connection);
            VisualPlace place = (VisualPlace) ((placeOrReplica instanceof VisualReplicaPlace)
                    ? ((VisualReplicaPlace) placeOrReplica).getMaster() : placeOrReplica);

            VisualConnection dualConsumingArc = visualModel.getConnection(place, transition);
            if (dualConsumingArc != null) {
                visualModel.remove(dualConsumingArc);
            }
            for (Replica replica : place.getReplicas()) {
                VisualConnection dualReplicaConsumingArc = visualModel.getConnection((VisualReplicaPlace) replica,
                        transition);
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
            } catch (InvalidConnectionException ignored) {
            }
        }
        return readArc;
    }

    public static VisualReadArc convertConsumingArcToReadArc(VisualModel visualModel, VisualConnection connection) {
        VisualReadArc readArc = null;
        VisualNode placeOrReplica;
        VisualNode transition;
        if (ConnectionUtils.isVisualConsumingArc(connection)) {
            placeOrReplica = connection.getFirst();
            if (placeOrReplica instanceof VisualReplicaPlace) {
                placeOrReplica = copyReplicaPlace(visualModel, (VisualReplicaPlace) placeOrReplica);
            }
            transition = connection.getSecond();
            // Remove consuming and dual producing arcs (including replicas)
            visualModel.remove(connection);
            VisualPlace place = (VisualPlace) ((placeOrReplica instanceof VisualReplicaPlace)
                    ? ((VisualReplicaPlace) placeOrReplica).getMaster() : placeOrReplica);

            VisualConnection dualProducingArc = visualModel.getConnection(transition, place);
            if (dualProducingArc != null) {
                visualModel.remove(dualProducingArc);
            }
            for (Replica replica : place.getReplicas()) {
                VisualConnection dualReplicaProducingArc = visualModel.getConnection(transition,
                        (VisualReplicaPlace) replica);

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
            } catch (InvalidConnectionException ignored) {
            }
        }
        return readArc;
    }

    public static Pair<VisualConnection, VisualConnection> convertReadArcTotDualArc(VisualModel visualModel,
            VisualReadArc readArc) {

        VisualNode first = readArc.getFirst();
        VisualNode second = readArc.getSecond();
        VisualConnection consumingArc = null;
        VisualConnection producingArc = null;
        if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace))
                && (second instanceof VisualTransition)) {

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
            } catch (InvalidConnectionException ignored) {
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

    public static Set<VisualConnection> collapseReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
        Set<VisualConnection> result = new HashSet<>();
        for (Connection connection : new HashSet<Connection>(visualModel.getConnections(replica))) {
            if (connection instanceof VisualConnection oldConnection) {
                VisualNode first = oldConnection.getFirst();
                VisualNode second = oldConnection.getSecond();
                LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getControlPoints(oldConnection);
                if (replica == first) {
                    first = replica.getMaster();
                    locationsInRootSpace.addFirst(replica.getRootSpacePosition());
                }
                if (replica == second) {
                    second = replica.getMaster();
                    locationsInRootSpace.addLast(replica.getRootSpacePosition());
                }
                visualModel.remove(oldConnection);
                try {
                    VisualConnection newConnection = (oldConnection instanceof VisualReadArc)
                            ? visualModel.connectUndirected(first, second)
                            : visualModel.connect(first, second);

                    newConnection.copyStyle(oldConnection);
                    ConnectionHelper.addControlPoints(newConnection, locationsInRootSpace);
                    result.add(newConnection);
                } catch (InvalidConnectionException ignored) {
                }
            }
        }
        return result;
    }

    public static VisualReplicaPlace replicateConnectedPlace(VisualModel visualModel, VisualConnection connection,
            Point2D offsetFromTransition) {

        VisualReplicaPlace replicaPlace = null;
        VisualTransition transition = null;
        VisualConnection replicaConnection = replicateConnectedPlace(visualModel, connection);
        if (replicaConnection != null) {
            if ((replicaConnection.getFirst() instanceof VisualReplicaPlace)
                    && ((replicaConnection.getSecond() instanceof VisualTransition))) {

                replicaPlace = (VisualReplicaPlace) replicaConnection.getFirst();
                transition = (VisualTransition) replicaConnection.getSecond();
            } else if ((replicaConnection.getSecond() instanceof VisualReplicaPlace)
                    && ((replicaConnection.getFirst() instanceof VisualTransition))) {

                replicaPlace = (VisualReplicaPlace) replicaConnection.getSecond();
                transition = (VisualTransition) replicaConnection.getFirst();
            }
            if ((replicaPlace != null) && (transition != null)) {
                replicaConnection.getGraphic().setDefaultControlPoints();
                replicaPlace.setRootSpacePosition(new Point2D.Double(
                        transition.getRootSpaceX() + offsetFromTransition.getX(),
                        transition.getRootSpaceY() + offsetFromTransition.getY()));
            }
        }
        return replicaPlace;
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
            Container container = Hierarchy.getNearestContainer(place, transition);
            VisualReplicaPlace replica = visualModel.createVisualReplica(place, VisualReplicaPlace.class, container);
            Boolean closerToSourceNode = null;
            if (connection.getFirst() instanceof VisualPlace) {
                closerToSourceNode = true;
            } else if (connection.getSecond() instanceof VisualPlace) {
                closerToSourceNode = false;
            }
            Point2D splitPointInRootSpace = ConnectionHelper.getReplicaPositionInRootSpace(connection, closerToSourceNode);
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
                result.copyStyle(connection);
            } catch (InvalidConnectionException ignored) {
            }
        }
        return result;
    }

    private static boolean areVisualDualArcs(VisualConnection connection1, VisualConnection connection2) {
        VisualConnection consumingArc = null;
        VisualConnection producingArc = null;
        if (ConnectionUtils.isVisualConsumingArc(connection1)) {
            consumingArc = connection1;
        } else if (ConnectionUtils.isVisualProducingArc(connection1)) {
            producingArc = connection1;
        }
        if (ConnectionUtils.isVisualConsumingArc(connection2)) {
            consumingArc = connection2;
        } else if (ConnectionUtils.isVisualProducingArc(connection2)) {
            producingArc = connection2;
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

}
