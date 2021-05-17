package org.workcraft.plugins.petri.utils;

import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.Hierarchy;

import java.util.HashSet;

public class ConnectionUtils extends org.workcraft.dom.visual.connections.ConnectionUtils {

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

    public static boolean isVisualProducingArc(VisualNode node) {
        if ((node instanceof VisualConnection) && !(node instanceof VisualReadArc)) {
            VisualConnection connection = (VisualConnection) node;
            return (connection.getFirst() instanceof VisualTransition)
                    && ((connection.getSecond() instanceof VisualPlace)
                    || (connection.getSecond() instanceof VisualReplicaPlace));
        }
        return false;
    }

    public static boolean isVisualConsumingArc(VisualNode node) {
        if ((node instanceof VisualConnection) && !(node instanceof VisualReadArc)) {
            VisualConnection connection = (VisualConnection) node;
            return ((connection.getFirst() instanceof VisualPlace)
                    || (connection.getFirst() instanceof VisualReplicaPlace))
                    && (connection.getSecond() instanceof VisualTransition);
        }
        return false;
    }

}
