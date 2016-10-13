package org.workcraft.plugins.stg.concepts;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.util.Hierarchy;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;

public class ConceptsLayout {

    private static final double xDiff = 2.5;
    private static final double yDiff = 2.5;
    private static final double instanceDiff = 1.0;
    private static final double centreXDiff = (xDiff * 2) + 2;
    private static final double replicaDiff = 1.5;

    public static void layout(VisualStg visualStg) {
        HashMap<String, HashSet<VisualComponent>> nodeMap = groupBySignal(visualStg);

        Point2D.Double centre = new Point2D.Double(0, 0);

        for (HashSet<VisualComponent> set : nodeMap.values()) {
            HashSet<VisualSignalTransition> plus = new HashSet<>();
            HashSet<VisualSignalTransition> minus = new HashSet<>();

            for (VisualComponent c : set) {
                if (c instanceof VisualPlace) {
                    if (visualStg.getMathName(c).endsWith("0")) {
                        c.setPosition(new Point2D.Double(centre.getX() - xDiff, centre.getY()));
                    } else {
                        c.setPosition(new Point2D.Double(centre.getX() + xDiff, centre.getY()));
                    }
                }

                if (c instanceof VisualSignalTransition) {
                    VisualSignalTransition t  = (VisualSignalTransition) c;
                    if (t.getDirection() == Direction.PLUS) {
                        plus.add(t);
                    } else if (t.getDirection() == Direction.MINUS) {
                        minus.add(t);
                    }
                }
            }
            Point2D.Double plusPos = new Point2D.Double(centre.getX(), centre.getY() - yDiff);
            for (VisualSignalTransition t : plus) {
                t.setPosition(new Point2D.Double(plusPos.getX(), plusPos.getY()));
                plusPos.setLocation(plusPos.getX(), plusPos.getY() - instanceDiff);
            }

            Point2D.Double minusPos = new Point2D.Double(centre.getX(), centre.getY() + yDiff);
            for (VisualSignalTransition t : minus) {
                t.setPosition(new Point2D.Double(minusPos.getX(), minusPos.getY()));
                minusPos.setLocation(minusPos.getX(), minusPos.getY() + instanceDiff);
            }

            centre.setLocation(centre.getX() + centreXDiff, centre.getY());
        }

        addReplicaPlaces(visualStg);
    }

    private static HashMap<String, HashSet<VisualComponent>> groupBySignal(VisualStg visualStg) {
        HashMap<String, HashSet<VisualComponent>> nodeMap = new HashMap<>();

        for (VisualSignalTransition t : visualStg.getVisualSignalTransitions()) {
            String signalName = t.getSignalName();
            if (nodeMap.containsKey(signalName)) {
                nodeMap.get(signalName).add(t);
            } else {
                HashSet<VisualComponent> newSet = new HashSet<>();
                newSet.add(t);
                nodeMap.put(signalName, newSet);
            }
        }

        for (VisualPlace p : visualStg.getVisualPlaces()) {
            String signalName = visualStg.getMathName(p);
            signalName = signalName.substring(0, signalName.length() - 1);
            if (nodeMap.containsKey(signalName)) {
                nodeMap.get(signalName).add(p);
            } else {
                HashSet<VisualComponent> newSet = new HashSet<>();
                newSet.add(p);
                nodeMap.put(signalName, newSet);
            }
        }

        return nodeMap;
    }

    private static void addReplicaPlaces(VisualStg visualStg) {
        Collection<VisualSignalTransition> transitions = visualStg.getVisualSignalTransitions();
        Container container = Hierarchy.getNearestContainer(new HashSet<Node>(transitions));

        for (VisualSignalTransition t : transitions) {
            HashSet<VisualReplicaPlace> replicas = new HashSet<>();
            ArrayList<VisualReplicaPlace> left = new ArrayList<>();
            ArrayList<VisualReplicaPlace> right = new ArrayList<>();
            boolean side = false;
            for (Connection c : visualStg.getConnections(t)) {
                if (c instanceof VisualReadArc) {
                    VisualReadArc a = (VisualReadArc) c;
                    VisualNode first = a.getFirst();
                    VisualReplicaPlace replicaPlace;
                    replicaPlace = visualStg.createVisualReplica((VisualPlace) first, container, VisualReplicaPlace.class);
                    replicas.add(replicaPlace);
                    if (side) {
                        right.add(replicaPlace);
                        side = !side;
                    } else {
                        left.add(replicaPlace);
                        side = !side;
                    }
                }
            }
            for (int i = 0; i < left.size(); i++) {
                VisualReplicaPlace r = left.get(i);
                if (left.size() == 1) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() - replicaDiff, t.getRootSpaceY()));
                } else if (i < (left.size() / 2)) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() - replicaDiff, t.getRootSpaceY() + (0.5 * (i + 1))));
                } else {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() - replicaDiff, t.getRootSpaceY() - (0.5 * ((i + 1) - (left.size() / 2)))));
                }
            }

            for (int i = 0; i < right.size(); i++) {
                VisualReplicaPlace r = right.get(i);
                if (right.size() == 1) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() + replicaDiff, t.getRootSpaceY()));
                } else if (i < (right.size() / 2)) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() + replicaDiff, t.getRootSpaceY() + (0.5 * (i + 1))));
                } else {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() + replicaDiff, t.getRootSpaceY() - (0.5 * ((i + 1) - (right.size() / 2)))));
                }
            }

            for (VisualReplicaPlace r : replicas) {
                try {
                    visualStg.remove(visualStg.getConnection(r.getMaster(), t));
                    visualStg.connectUndirected(r, t);
                } catch (InvalidConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
