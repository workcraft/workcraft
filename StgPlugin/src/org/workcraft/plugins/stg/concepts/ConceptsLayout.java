package org.workcraft.plugins.stg.concepts;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;

public class ConceptsLayout {

    private static final double xDiff = 2.5;
    private static final double yDiff = 2.5;
    private static final double instanceDiff = 1.0;
    private static final double centreXDiff = (xDiff * 2) + 2;

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

}
