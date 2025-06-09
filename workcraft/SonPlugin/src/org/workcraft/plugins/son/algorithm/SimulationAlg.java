package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.exception.UnboundedException;
import org.workcraft.plugins.son.util.Phase;
import org.workcraft.plugins.son.util.Step;

import java.util.*;

public class SimulationAlg extends RelationAlgorithm {

    private final SON net;
    private final BSONAlg bsonAlg;
    private final SONAlg sonAlg;

    private final Collection<ONGroup> upperGroups;
    private final Collection<ONGroup> lowerGroups;

    public SimulationAlg(SON net) {
        super(net);
        this.net = net;
        bsonAlg = new BSONAlg(net);
        sonAlg = new SONAlg(net);

        upperGroups = bsonAlg.getUpperGroups(net.getGroups());
        lowerGroups = bsonAlg.getLowerGroups(net.getGroups());
    }

    //get SON initial marking
    public Map<PlaceNode, Boolean> getInitialMarking() {
        HashMap<PlaceNode, Boolean> result = new HashMap<>();

        Collection<PlaceNode> initialM = sonAlg.getSONInitial();

        for (PlaceNode c : net.getPlaceNodes()) {
            if (initialM.contains(c)) {
                result.put(c, true);
            } else {
                result.put(c, false);
            }
        }

        return result;
    }

    //get SON final marking
    public Map<PlaceNode, Boolean> getFinalMarking() {
        HashMap<PlaceNode, Boolean> result = new HashMap<>();

        Collection<PlaceNode> finalM = sonAlg.getSONFinal();

        for (PlaceNode c : net.getPlaceNodes()) {
            if (finalM.contains(c)) {
                result.put(c, true);
            } else {
                result.put(c, false);
            }
        }

        return result;
    }

    /**
     * return minimal execution set for a given node.
     * contain other nodes which have synchronous and PRE- relation with the selected one.
     */
    public Step getMinFire(TransitionNode e, Collection<Path> sync, Step step, boolean isRev) {
        Step result = null;
        if (!isRev) {
            result = getForwordMinFire(e, sync, step);
        } else {
            result = getRevMinFire(e, sync, step);
        }

        return result;
    }

    private Step getForwordMinFire(TransitionNode e, Collection<Path> sync, Step step) {
        Step result = new Step();
        Step u = new Step();
        Stack<TransitionNode> stack = new Stack<>();
        u.addAll(step);

        if (e != null) {
            stack.push(e);
            while (!stack.empty()) {
                e = stack.pop();
                if (!result.contains(e)) {
                    result.add(e);
                    u.remove(e);
                }
                //event in sync cycle belongs to the result
                for (Path cycle : sync) {
                    if (cycle.contains(e)) {
                        for (Node e2 : cycle) {
                            if (e2 instanceof TransitionNode && u.contains(e2)) {
                                u.remove(e2);
                                stack.push((TransitionNode) e2);
                            } else if (!step.contains(e2)) {
                                throw new RuntimeException("algorithm error: unenabled event in sync cycle" + net.getNodeReference(e2));
                            }
                        }
                    }
                }
                //event which is the preset w.r.t weak causality, of selected event belongs to the result.
                if (!getPreAsynEvents(e).isEmpty()) {
                    for (TransitionNode e3 : getPreAsynEvents(e)) {
                        if (u.contains(e3)) {
                            u.remove(e3);
                            stack.push(e3);
                        }
                    }
                }
            }
        }
        return result;
    }

    private Step getRevMinFire(TransitionNode e, Collection<Path> sync, Step step) {
        Step result = new Step();
        Step u = new Step();
        Stack<TransitionNode> stack = new Stack<>();
        u.addAll(step);

        if (e != null) {
            stack.push(e);
            while (!stack.empty()) {
                e = stack.pop();
                if (!result.contains(e)) {
                    result.add(e);
                    u.remove(e);
                }

                for (Path cycle : sync) {
                    if (cycle.contains(e)) {
                        for (Node e2 : cycle) {
                            if (e2 instanceof TransitionNode && u.contains(e2)) {
                                u.remove(e2);
                                stack.push((TransitionNode) e2);
                            } else if (!step.contains(e2)) {
                                throw new RuntimeException("algorithm error: unenabled event in sync cycle" + net.getNodeReference(e2));
                            }
                        }
                    }
                }
                if (!getPostAsynEvents(e).isEmpty()) {
                    for (TransitionNode e3 : getPostAsynEvents(e)) {
                        if (u.contains(e3)) {
                            u.remove(e3);
                            stack.push(e3);
                        }
                    }
                }
            }
        }
        return result;
    }

    public final Step getEnabledNodes(Collection<Path> sync, Map<Condition, Collection<Phase>> phases, boolean isRev) {
        Step result = null;
        if (!isRev) {
            result = getEnabled(sync, phases);
        } else {
            result = getRevEnabled(sync, phases);
        }

        return result;
    }

    private boolean isONEnabled(TransitionNode e) {
        if (net.getPreset((MathNode) e).isEmpty()) {
            return false;
        }

        for (MathNode n : net.getPreset((MathNode) e)) {
            if (n instanceof Condition) {
                if (!((Condition) n).isMarked()) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isBSONEnabled(TransitionNode e, Map<Condition, Collection<Phase>> phases) {
        //if e is upper event, e is BSON enabled if every condition in the maximal phases of e is marked
        for (ONGroup group : upperGroups) {
            if (group.getComponents().contains(e)) {
                for (Node pre : getPrePNSet(e)) {
                    Condition c = (Condition) pre;
                    Collection<Phase> phase = getActivatedPhases(phases.get(c));
                    Collection<Condition> max = bsonAlg.getMaximalPhase(phase);
                    for (Condition c2 : max) {
                        if (!c2.isMarked()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        //if e is lower event, e is BSON enabled if every e's upper condition is marked
        for (ONGroup group : lowerGroups) {
            if (group.getComponents().contains(e)) {
                for (Condition c : bsonAlg.getUpperConditions(e)) {
                    if (!c.isMarked()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected ArrayList<Phase> getActivatedPhases(Collection<Phase> phases) {
        ArrayList<Phase> result = new ArrayList<>();
        for (Phase phase : phases) {
            for (Condition c : phase) {
                if (c.isMarked()) {
                    result.add(phase);
                    break;
                }
            }
        }
        return result;
    }

    private Step getEnabled(Collection<Path> sync, Map<Condition, Collection<Phase>> phases) {
        Step result = new Step();
        Collection<Node> del = new HashSet<>();
        Stack<TransitionNode> stack = new Stack<>();

        //ON and BSON enabled
        for (TransitionNode e : net.getTransitionNodes()) {
            if (isONEnabled(e) && isBSONEnabled(e, phases)) {
                result.add(e);
            }
        }

        //Sync enabled
        for (Path cycle : sync) {
            for (Node n : cycle) {
                if (n instanceof TransitionNode && !result.contains(n)) {
                    del.addAll(cycle);
                    break;
                }
            }
        }

        //Aync enabled
        for (TransitionNode e : result) {
            LinkedList<Node> visit = new LinkedList<>();
            stack.push(e);

            while (!stack.isEmpty()) {
                TransitionNode e1 = stack.peek();
                visit.add(e1);

                TransitionNode e2 = null;
                for (MathNode pre : net.getPreset((MathNode) e1)) {
                    if (pre instanceof ChannelPlace) {
                        if (!((ChannelPlace) pre).isMarked()) {
                            for (MathNode pre2 : net.getPreset(pre)) {
                                if (!visit.contains(pre2)) {
                                    if (!result.contains(pre2) || del.contains(pre2)) {
                                        visit.add(e2);
                                        del.addAll(visit);
                                        visit.removeLast();
                                        break;
                                    } else if (!visit.contains(pre2)) {
                                        e2 = (TransitionNode) pre2;
                                        stack.push(e2);
                                    }
                                }
                            }
                        }
                    }
                }
                if (e2 == null) {
                    while (!stack.isEmpty()) {
                        TransitionNode e3 = stack.peek();
                        if (!visit.isEmpty() && (e3 == visit.peekLast())) {
                            stack.pop();
                            visit.removeLast();
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        result.removeAll(del);
        return result;
    }

    //reverse simulation
    private boolean isRevONEnabled(TransitionNode e) {
        if (net.getPostset((MathNode) e).isEmpty()) {
            return false;
        }

        for (Node n : net.getPostset((MathNode) e)) {
            if (n instanceof Condition) {
                if (!((Condition) n).isMarked()) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isRevBSONEnabled(TransitionNode e, Map<Condition, Collection<Phase>> phases) {
        //if e is upper event, e is BSON unfire enabled if every condition in the minimal phases of e is marked
        for (ONGroup group : upperGroups) {
            if (group.getComponents().contains(e)) {
                for (Node post : getPostPNSet(e)) {
                    Condition c = (Condition) post;
                    Collection<Phase> phase = getActivatedPhases(phases.get(c));
                    Collection<Condition> min = bsonAlg.getMinimalPhase(phase);
                    for (Condition c2 : min) {
                        if (!c2.isMarked()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        //if e is lower event, e is BSON enabled if every e's upper condition is marked
        for (ONGroup group : lowerGroups) {
            if (group.getComponents().contains(e)) {
                for (Condition c : bsonAlg.getUpperConditions(e)) {
                    if (!c.isMarked()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private Step getRevEnabled(Collection<Path> sync, Map<Condition, Collection<Phase>> phases) {
        Step result = new Step();
        Collection<Node> del = new HashSet<>();
        Stack<TransitionNode> stack = new Stack<>();

        //ON and BSON enabled
        for (TransitionNode e : net.getTransitionNodes()) {
            if (isRevONEnabled(e) && isRevBSONEnabled(e, phases)) {
                result.add(e);
            }
        }

        //Sync enabled
        for (Path cycle : sync) {
            for (Node n : cycle) {
                if (n instanceof TransitionNode && !result.contains(n)) {
                    del.addAll(cycle);
                    break;
                }
            }
        }

        //Aync enabled
        for (TransitionNode e : result) {
            LinkedList<Node> visit = new LinkedList<>();
            stack.push(e);

            while (!stack.isEmpty()) {
                TransitionNode e1 = stack.peek();
                visit.add(e1);

                TransitionNode e2 = null;
                for (MathNode post : net.getPostset((MathNode) e1)) {
                    if (post instanceof ChannelPlace) {
                        if (!((ChannelPlace) post).isMarked()) {
                            for (MathNode post2 : net.getPostset(post)) {
                                if (!visit.contains(post2)) {
                                    if (!result.contains(post2) || del.contains(post2)) {
                                        visit.add(e2);
                                        del.addAll(visit);
                                        visit.removeLast();
                                        break;
                                    } else if (!visit.contains(post2)) {
                                        e2 = (TransitionNode) post2;
                                        stack.push(e2);
                                    }
                                }
                            }
                        }
                    }
                }
                if (e2 == null) {
                    while (!stack.isEmpty()) {
                        TransitionNode e3 = stack.peek();
                        if (!visit.isEmpty() && (e3 == visit.peekLast())) {
                            stack.pop();
                            visit.removeLast();
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        result.removeAll(del);
        return result;
    }

    public void setMarking(Step step, Map<Condition, Collection<Phase>> phases, boolean isRev) throws UnboundedException {
        if (!isRev) {
            fire(step, phases);
        } else {
            revFire(step, phases);
        }
    }

    /**
     * token setting after forward fire.
     * @throws UnboundedException
     */
    private void fire(Step step, Map<Condition, Collection<Phase>> phases) throws UnboundedException {
        //marking for ON and CSON
        for (TransitionNode e : step) {
            for (MathNode post : net.getPostset((MathNode) e)) {
                if ((post instanceof PlaceNode) && net.getSONConnectionType((MathNode) e, post) != Semantics.SYNCLINE) {
                    if (((PlaceNode) post).isMarked()) {
                        throw new UnboundedException(net.getNodeReference(post), post);
                    } else {
                        ((PlaceNode) post).setMarked(true);
                    }
                }
            }
        }

        for (TransitionNode e : step) {
            for (MathNode pre : net.getPreset((MathNode) e)) {
                if ((pre instanceof PlaceNode) && net.getSONConnectionType((MathNode) e, pre) != Semantics.SYNCLINE) {
                    ((PlaceNode) pre).setMarked(false);
                }
            }
        }

        for (TransitionNode e : step) {
            //marking for BSON
            for (MathNode pre : net.getPreset((MathNode) e)) {
                //if e is upper event, remove marking for maximal phase of pre{e}.
                if (bsonAlg.isUpperCondition(pre)) {
                    Condition c = (Condition) pre;
                    Collection<Condition> maxSet = bsonAlg.getMaximalPhase(phases.get(c));
                    //backward checking for all upper conditions, if there has no marked condition, remove the token
                    boolean hasMarking = false;
                    for (Condition c2 : maxSet) {
                        for (Condition c3 : bsonAlg.getUpperConditions(c2)) {
                            if (c3.isMarked()) {
                                hasMarking = true;
                            }
                        }
                        if (!hasMarking) {
                            c2.setMarked(false);
                        }
                    }
                }
            }

            for (MathNode post : net.getPostset((MathNode) e)) {
                //if e is upper event, set marking for every minimal phase of post{e}.
                if (bsonAlg.isUpperCondition(post)) {
                    Condition c = (Condition) post;
                    Collection<Condition> minSet = bsonAlg.getMinimalPhase(phases.get(c));
                    for (Condition min : minSet) {
                        if (isInitial(min) && !min.isMarked()) {
                            min.setMarked(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * token setting after reverse fire.
     * @throws UnboundedException
     */
    private void revFire(Step step, Map<Condition, Collection<Phase>> phases) throws UnboundedException {

        //marking for ON and CSON
        for (TransitionNode e : step) {
            for (MathNode pre : net.getPreset((MathNode) e)) {
                if ((pre instanceof PlaceNode) && net.getSONConnectionType((MathNode) e, pre) != Semantics.SYNCLINE) {
                    if (((PlaceNode) pre).isMarked()) {
                        throw new UnboundedException(net.getNodeReference(pre), pre);
                    } else {
                        ((PlaceNode) pre).setMarked(true);
                    }
                }
            }
        }

        for (TransitionNode e : step) {
            for (MathNode post : net.getPostset((MathNode) e)) {
                if ((post instanceof PlaceNode) && net.getSONConnectionType((MathNode) e, post) != Semantics.SYNCLINE) {
                    ((PlaceNode) post).setMarked(false);
                }
            }
        }

        for (TransitionNode e : step) {
            //marking for BSON
            for (MathNode post : net.getPostset((MathNode) e)) {
                //if e is upper event, remove marking for maximal phase of pre{e}.
                if (bsonAlg.isUpperCondition(post)) {
                    Condition c = (Condition) post;
                    Collection<Condition> minSet = bsonAlg.getMinimalPhase(phases.get(c));
                    //backward checking for all upper conditions, if there has no marked condition, remvoe the token
                    boolean hasMarking = false;
                    for (Condition c2 : minSet) {
                        for (Condition c3 : bsonAlg.getUpperConditions(c2)) {
                            if (c3.isMarked()) {
                                hasMarking = true;
                            }
                        }
                        if (!hasMarking) {
                            c2.setMarked(false);
                        }
                    }
                }
            }

            for (MathNode pre : net.getPreset((MathNode) e)) {
                //if e is upper event, set marking for every minimal phase of post{e}.
                if (bsonAlg.isUpperCondition(pre)) {
                    Condition c = (Condition) pre;
                    Collection<Condition> maxSet = bsonAlg.getMaximalPhase(phases.get(c));
                    for (Condition max : maxSet) {
                        if (isFinal(max) && !max.isMarked()) {
                            max.setMarked(true);
                        }
                    }
                }
            }
        }
    }

}
