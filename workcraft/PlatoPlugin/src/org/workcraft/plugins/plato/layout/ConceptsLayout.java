package org.workcraft.plugins.plato.layout;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class ConceptsLayout {

    private static final double xDiff = 2.5;
    private static final double yDiff = 2.5;
    private static final double instanceDiff = 1.0;
    private static final double centreXDiff = (xDiff * 2) + 2;
    private static final double replicaDiff = 1.5;
    private static int prevSize = 0;

    public static void layout(VisualStg visualStg) {
        try {
            HashMap<Signal.Type, HashMap<String, HashSet<VisualComponent>>> typeMap = groupBySignalType(visualStg);

            double centreX = 0.0;
            double centreY = 0.0;

            ArrayList<Signal.Type> typeList = new ArrayList<>();
            typeList.add(Signal.Type.INPUT);
            typeList.add(Signal.Type.INTERNAL);
            typeList.add(Signal.Type.OUTPUT);

            for (Signal.Type t: typeList) {
                if (typeMap.containsKey(t)) {
                    centreX = arrangeNodes(visualStg, typeMap.get(t), centreX, centreY);
                    centreY += (yDiff * 2) + 2;
                }
            }

            createConsistencyLoops(visualStg);
            addReplicaPlaces(visualStg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double arrangeNodes(VisualStg visualStg, HashMap<String, HashSet<VisualComponent>> nodeMap, double x, double y) {
        double leftmost = 0.0;
        double rightmost = 0.0;
        int mapSize = nodeMap.size();

        if ((x != 0) && (nodeMap.size() > 1)) {
            x -= ((nodeMap.size() - 1) / 2.0) * centreXDiff;
            if (prevSize != 0) {
                if (mapSize % 2 == 0) {
                    x -= centreXDiff / 2;
                }
            }
        }
        prevSize = nodeMap.size();

        Point2D.Double centre = new Point2D.Double(x, y);

        for (HashSet<VisualComponent> set : nodeMap.values()) {
            HashSet<VisualSignalTransition> plus = new HashSet<>();
            HashSet<VisualSignalTransition> minus = new HashSet<>();

            for (VisualComponent c : set) {
                if (c instanceof VisualPlace) {
                    if (visualStg.getMathName(c).endsWith("0")) {
                        c.setNamePositioning(Positioning.RIGHT);
                        c.setPosition(new Point2D.Double(centre.getX() - xDiff, centre.getY()));
                        if ((c.getPosition().getX() < leftmost) || (leftmost == 0.0)) {
                            leftmost = c.getPosition().getX();
                        }
                    } else {
                        c.setNamePositioning(Positioning.LEFT);
                        c.setPosition(new Point2D.Double(centre.getX() + xDiff, centre.getY()));
                        if ((c.getPosition().getX() > rightmost) || (rightmost == 0.0)) {
                            rightmost = c.getPosition().getX();
                        }
                    }
                }

                if (c instanceof VisualSignalTransition t) {
                    if (t.getDirection() == SignalTransition.Direction.PLUS) {
                        plus.add(t);
                    } else if (t.getDirection() == SignalTransition.Direction.MINUS) {
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
        double diff = rightmost - leftmost;
        return leftmost + (diff / 2);
    }

    private static void createConsistencyLoops(VisualStg visualStg) {
        Collection<VisualConnection> set = visualStg.getVisualConnections();

        for (VisualConnection c :  set) {
            if (!(c instanceof VisualReadArc)) {
                VisualNode first = c.getFirst();
                VisualNode second = c.getSecond();
                c.setConnectionType(ConnectionType.BEZIER);
                Bezier bezier = (Bezier) c.getGraphic();
                BezierControlPoint[] points = bezier.getBezierControlPoints();

                if (first instanceof VisualSignalTransition f) {
                    if (f.getDirection() == SignalTransition.Direction.PLUS) {
                        points[0].setPosition(new Point2D.Double(f.getCenter().getX() + 1.5, f.getCenter().getY()));
                        points[1].setPosition(new Point2D.Double(second.getCenter().getX(), second.getCenter().getY() - 1.5));
                    } else {
                        points[0].setPosition(new Point2D.Double(f.getCenter().getX() - 1.5, f.getCenter().getY()));
                        points[1].setPosition(new Point2D.Double(second.getCenter().getX(), second.getCenter().getY() + 1.5));
                    }
                } else {
                    VisualSignalTransition s = (VisualSignalTransition) second;
                    if (s.getDirection() == SignalTransition.Direction.PLUS) {
                        points[0].setPosition(new Point2D.Double(first.getCenter().getX(), first.getCenter().getY() - 1.5));
                        points[1].setPosition(new Point2D.Double(s.getCenter().getX() - 1.5, s.getCenter().getY()));
                    } else {
                        points[0].setPosition(new Point2D.Double(first.getCenter().getX(), first.getCenter().getY() + 1.5));
                        points[1].setPosition(new Point2D.Double(s.getCenter().getX() + 1.5, s.getCenter().getY()));
                    }
                }
            }
        }
    }

    private static HashMap<Signal.Type, HashMap<String, HashSet<VisualComponent>>> groupBySignalType(VisualStg visualStg) {
        HashMap<Signal.Type, HashMap<String, HashSet<VisualComponent>>> typeMap = new HashMap<>();

        for (VisualSignalTransition t : visualStg.getVisualSignalTransitions()) {
            String signalName = t.getSignalName();
            Signal.Type signalType = t.getSignalType();
            if (typeMap.containsKey(signalType)) {
                HashMap<String, HashSet<VisualComponent>> nodeMap = typeMap.get(signalType);
                if (nodeMap.containsKey(signalName)) {
                    nodeMap.get(signalName).add(t);
                } else {
                    HashSet<VisualComponent> newSet = new HashSet<>();
                    newSet.add(t);
                    nodeMap.put(signalName, newSet);
                }
            } else {
                HashSet<VisualComponent> newSet = new HashSet<>();
                newSet.add(t);
                HashMap<String, HashSet<VisualComponent>> nodeMap = new HashMap<>();
                nodeMap.put(signalName, newSet);
                typeMap.put(signalType, nodeMap);
            }
        }

        for (VisualPlace p : visualStg.getVisualPlaces()) {
            String signalName = visualStg.getMathName(p);
            signalName = signalName.substring(0, signalName.length() - 1);
            Signal.Type signalType = findSignalType(signalName, typeMap);
            if (typeMap.containsKey(signalType)) {
                HashMap<String, HashSet<VisualComponent>> nodeMap = typeMap.get(signalType);
                if (nodeMap.containsKey(signalName)) {
                    nodeMap.get(signalName).add(p);
                } else {
                    HashSet<VisualComponent> newSet = new HashSet<>();
                    newSet.add(p);
                    nodeMap.put(signalName, newSet);
                }
            } else {
                HashSet<VisualComponent> newSet = new HashSet<>();
                newSet.add(p);
                HashMap<String, HashSet<VisualComponent>> nodeMap = new HashMap<>();
                nodeMap.put(signalName, newSet);
                typeMap.put(signalType, nodeMap);
            }
        }
        return typeMap;
    }

    private static Signal.Type findSignalType(String signalName, HashMap<Signal.Type, HashMap<String, HashSet<VisualComponent>>> typeMap) {
        for (Signal.Type t: typeMap.keySet()) {
            HashMap<String, HashSet<VisualComponent>> nodeMap = typeMap.get(t);
            for (String s : nodeMap.keySet()) {
                if (signalName.equals(s)) {
                    return t;
                }
            }
        }
        return null;
    }

    private static void addReplicaPlaces(VisualStg visualStg) {
        Collection<VisualSignalTransition> transitions = visualStg.getVisualSignalTransitions();
        Container container = Hierarchy.getNearestContainer(new HashSet<>(transitions));

        for (VisualSignalTransition t : transitions) {
            HashSet<VisualReplicaPlace> replicas = new HashSet<>();
            ArrayList<VisualReplicaPlace> left = new ArrayList<>();
            ArrayList<VisualReplicaPlace> right = new ArrayList<>();
            boolean side = false;
            for (Connection c : visualStg.getConnections(t)) {
                if (c instanceof VisualReadArc a) {
                    VisualNode first = a.getFirst();
                    VisualReplicaPlace replicaPlace;
                    replicaPlace = visualStg.createVisualReplica((VisualPlace) first, VisualReplicaPlace.class, container);
                    replicas.add(replicaPlace);
                    if (side) {
                        right.add(replicaPlace);
                    } else {
                        left.add(replicaPlace);
                    }
                    side = !side;
                }
            }
            for (int i = 0; i < left.size(); i++) {
                VisualReplicaPlace r = left.get(i);
                if (left.size() == 1) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() - replicaDiff, t.getRootSpaceY()));
                } else if (i < (left.size() / 2)) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() - replicaDiff, t.getRootSpaceY() + (0.5 * (i + 1))));
                } else {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() - replicaDiff, t.getRootSpaceY() - (0.5 * ((i + 1) - (left.size() / 2.0)))));
                }
            }

            for (int i = 0; i < right.size(); i++) {
                VisualReplicaPlace r = right.get(i);
                if (right.size() == 1) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() + replicaDiff, t.getRootSpaceY()));
                } else if (i < (right.size() / 2)) {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() + replicaDiff, t.getRootSpaceY() + (0.5 * (i + 1))));
                } else {
                    r.setRootSpacePosition(new Point2D.Double(t.getRootSpaceX() + replicaDiff, t.getRootSpaceY() - (0.5 * ((i + 1) - (right.size() / 2.0)))));
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
