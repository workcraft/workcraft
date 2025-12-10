package org.workcraft.plugins.cpog.untangling;

import org.jbpt.petri.*;
import org.jbpt.petri.unfolding.BPNode;
import org.jbpt.petri.unfolding.Condition;
import org.jbpt.petri.unfolding.Event;
import org.jbpt.petri.untangling.IProcess;
import org.jbpt.petri.untangling.ReductionBasedRepresentativeUntangling;
import org.jbpt.petri.untangling.SignificanceCheckType;
import org.jbpt.petri.untangling.UntanglingSetup;
import org.workcraft.plugins.cpog.commands.PetriToCpogParameters;
import org.workcraft.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Untanglings {

    private final NetSystem sys;
    private final LinkedList<Place> p;
    private final LinkedList<Transition> t;
    private final UntanglingSetup setup;
    private ReductionBasedRepresentativeUntangling untangling;
    private final ArrayList<PartialOrder> partialOrders;

    public Untanglings(PetriToCpogParameters settings) {
        this.sys = new NetSystem();
        this.p = new LinkedList<>();
        this.t = new LinkedList<>();
        this.setup = new UntanglingSetup();
        this.partialOrders = new ArrayList<>();

        // settings
        this.setup.ISOMORPHISM_REDUCTION = settings.isIsomorphism();
        this.setup.REDUCE = settings.isReduce();
        switch (settings.getSignificance()) {
            case 0 :
                this.setup.SIGNIFICANCE_CHECK = SignificanceCheckType.EXHAUSTIVE;
                break;
            case 1 :
                this.setup.SIGNIFICANCE_CHECK = SignificanceCheckType.HASHMAP_BASED;
                break;
            case 2 :
                this.setup.SIGNIFICANCE_CHECK = SignificanceCheckType.TREE_OF_RUNS;
                break;
            default:
                break;
        }

    }

    /** adds place inside the conversion system **/
    public boolean addPlace(String placeName) {
        return p.add(new Place(placeName));
    }

    /** adds token inside a place inside the conversion system **/
    public boolean insertTokens(String placeName, int tokens) {

        for (Place place : p) {
            if (place.getLabel().equals(placeName)) {
                sys.putTokens(place, tokens);

                // debug printing: tokens inserted inside the place
                // System.out.println(place.getLabel() + " = " + tokens);

                return true;
            }
        }

        return false;
    }

    /** adds transition inside the conversion system **/
    public boolean addTransition(String transitionName) {
        return t.add(new Transition(transitionName));
    }

    /** adds a connection from a place to a transition **/
    public boolean placeToTransition(String node1, String node2) {
        for (Place place : p) {
            // checking existence of the place
            if (!place.getName().equals(node1)) continue;
            // checking existence of the transition
            for (Transition transition : t) {
                if (transition.getName().equals(node2)) {
                    // adding arc to the system
                    sys.addFlow(place, transition);
                    // debug: printing connection added
                    // System.out.println(place.getName() + " -> " + transition.getName());
                    return true;
                }
            }
        }
        // if the two nodes are not present the connection
        // is not inserted
        return false;
    }

    /** adds a connection from a transition to a place  **/
    public boolean transitionToPlace(String node1, String node2) {
        for (Transition transition : t) {
            // checking existence of the place
            if (!transition.getName().equals(node1)) continue;
            // checking existence of the transition
            for (Place place : p) {
                if (place.getName().equals(node2)) {
                    // adding arc to the system
                    sys.addFlow(transition, place);
                    // debug: printing connection added
                    // System.out.println(transition.getName() + " -> " + place.getName());
                    return true;
                }
            }
        }
        // if the two nodes are not present the connection
        // is not inserted
        return false;
    }

    /** converts the Petri net introduced into multiple *
     *  processes which compose the untangling         **/
    public boolean startConversion() {

        // starting conversion
        untangling = new ReductionBasedRepresentativeUntangling(sys, setup);

        // if Petri Net is not safe, stop the conversion
        if (!untangling.isSafe()) {
            LogUtils.logError("Untangling cannot be constructed because the Petri Net is not safe.");
            return false;
        }
        // checking correct execution of conversion
        for (IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi : untangling.getProcesses()) {

            if (!pi.getOccurrenceNet().getVertices().isEmpty()) {
                // printing out how many processes are needed to represent the untangling representation
                LogUtils.logInfo("Number of untangled processes: " + untangling.getProcesses().size());
                return true;
            }
        }

        return false;
    }

    /** converts the set of processes that compose the *
     *  untangling into a set of partial order graph  **/
    public ArrayList<PartialOrder> getPartialOrders(PetriToCpogParameters settings) {

        for (IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi : untangling.getProcesses()) {

            PartialOrder process = new PartialOrder();
            NodeList places = new NodeList();
            HashMap<Integer, UntanglingNode> idToPlacesMap = new HashMap<>();
            NodeList transitions = new NodeList();
            HashMap<Integer, UntanglingNode> idToTransitionsMap = new HashMap<>();

            // adding places into the places map
            for (Place place : pi.getOccurrenceNet().getPlaces()) {
                UntanglingNode untanglingNode = places.addNode(place);
                idToPlacesMap.put(untanglingNode.getId(), untanglingNode);
            }

            // adding transitions into the transitions map
            for (Transition transition : pi.getOccurrenceNet().getTransitions()) {
                UntanglingNode untanglingNode = transitions.addNode(transition);
                idToTransitionsMap.put(untanglingNode.getId(), untanglingNode);
            }

            // sorting and renaming transitions
            transitions.sort();
            transitions.rename();

            // sorting and renaming places
            if (!settings.isRemoveNodes()) {
                places.sort();
                places.rename();
            }

            // connecting transitions while skipping the places
            if (settings.isRemoveNodes()) {
                connectTransitionsOnly(pi, process, idToTransitionsMap);
            } else {
                // places need to be present
                connectTransitionsAndPlaces(pi, process, idToPlacesMap, idToTransitionsMap);
            }
            partialOrders.add(process);
        }

        return partialOrders;

    }

    /** Connects nodes and transitions in order to build the partial order **/
    private void connectTransitionsAndPlaces(
            IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi,
            PartialOrder process, HashMap<Integer, UntanglingNode> idToPlacesMap, HashMap<Integer,
            UntanglingNode> idToTransitionsMap) {

        for (Flow edge : pi.getOccurrenceNet().getEdges()) {
            Node source = edge.getSource();
            Node target = edge.getTarget();
            if (edge.getSource() instanceof Place) {
                // place to transition connection
                UntanglingEdge connection = connectNodes(source, target,
                        idToPlacesMap, idToTransitionsMap);
                process.add(connection);
            } else {
                // transition to place connection
                UntanglingEdge connection = connectNodes(source, target,
                        idToTransitionsMap, idToPlacesMap);

                // debug printing : connection
                // System.out.println(connection.getFirst().getLabel() + " -> " + connection.getSecond().getLabel());

                process.add(connection);
            }

        }
    }

    /** Connect transitions in order to build a partial order. Places are skipped **/
    private void connectTransitionsOnly(
            IProcess<BPNode, Condition, Event, Flow, Node, Place, Transition, Marking> pi,
            PartialOrder process, HashMap<Integer, UntanglingNode> idToTransitionMap) {

        for (Flow edge1 : pi.getOccurrenceNet().getEdges()) {
            if (!(edge1.getSource() instanceof Transition)) continue;
            for (Flow edge2 : pi.getOccurrenceNet().getEdges()) {

                String sourceName = edge2.getSource().getLabel();
                String targetName = edge1.getTarget().getLabel();

                if (!sourceName.equals(targetName)) continue;

                Node source = edge1.getSource();
                Node target = edge2.getTarget();
                UntanglingEdge connection = connectNodes(source, target,
                        idToTransitionMap, idToTransitionMap);

                // debug printing : connection
                // System.out.println(connection.getFirst().getLabel() + " -> " + connection.getSecond().getLabel());

                process.add(connection);
            }
        }
    }

    /** Connect two nodes  **/
    private UntanglingEdge connectNodes(Node source, Node target,
            HashMap<Integer, UntanglingNode> idToFirstListMap,
            HashMap<Integer, UntanglingNode> idToSecondListMap) {

        int sourceId = Integer.parseInt(source.getLabel().replaceAll(".*-", ""));
        int targetId = Integer.parseInt(target.getLabel().replaceAll(".*-", ""));
        UntanglingNode first = idToFirstListMap.get(sourceId);
        UntanglingNode second = idToSecondListMap.get(targetId);

        return new UntanglingEdge(first, second);

    }
}
