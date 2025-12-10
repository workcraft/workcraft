package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.util.Phase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ErrorTracingAlg extends SimulationAlg {

    private final SON net;
    private final BSONAlg bsonAlg;

    public ErrorTracingAlg(SON net) {
        super(net);
        bsonAlg = new BSONAlg(net);
        this.net = net;
    }

    //Forward error tracing
    public void setErrNum(Collection<TransitionNode> fireList, Collection<Path> sync, Map<Condition, Collection<Phase>> phases, boolean isLower) {

        while (!fireList.isEmpty()) {
            Collection<TransitionNode> removeList = new ArrayList<>();

            for (TransitionNode e : fireList) {
                if (!net.getSONConnectionTypes((MathNode) e).contains(Semantics.SYNCLINE)) {
                    //set number from the very first asynchronous event
                    if (getPreAsynEvents(e).isEmpty() || !hasCommonElements(fireList, getPreAsynEvents(e))) {
                        setAsynErrNum(e, phases, isLower);
                        fireList.remove(e);
                        break;
                    }
                }

                //set number for the synchronous events with no pre-async-events
                boolean b = false;
                for (Path cycle : sync) {
                    if (cycle.contains(e)) {
                        Collection<TransitionNode> fireList2 = new ArrayList<>();
                        Collection<TransitionNode> eventCycle = new ArrayList<>();

                        fireList2.addAll(fireList);
                        fireList2.removeAll(cycle);
                        boolean hasPreAsyn = false;
                        for (Node n: cycle) {
                            if (n instanceof TransitionNode) {
                                eventCycle.add((TransitionNode) n);
                                if (!getPreAsynEvents((TransitionNode) n).isEmpty()
                                        && hasCommonElements(fireList2, getPreAsynEvents((TransitionNode) n))) {
                                    hasPreAsyn = true;
                                }
                            }
                        }
                        if (!hasPreAsyn) {
                            setSyncErrNum(eventCycle, phases, isLower);
                            removeList.addAll(eventCycle);
                            b = true;
                            break;
                        }
                    }
                }
                if (b) {
                    break;
                }
            }
            fireList.removeAll(removeList);
        }
    }

    private boolean hasCommonElements(Collection<TransitionNode> cycle1, Collection<TransitionNode> cycle2) {
        for (Node n : cycle1) {
            if (cycle2.contains(n)) {
                return true;
            }
        }
        for (Node n : cycle2) {
            if (cycle1.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private void setAsynErrNum(TransitionNode e, Map<Condition, Collection<Phase>> phases, boolean isLower) {
        int err = 0;
        if (e.isFaulty()) {
            err++;
        }
        //get err number from lower conditions and channel places
        for (MathNode pre : net.getPreset((MathNode) e)) {
            if (pre instanceof PlaceNode) {
                err += ((PlaceNode) pre).getErrors();
            }
        }

        for (MathNode post: net.getPostset((MathNode) e)) {
            if (post instanceof Condition) {
                ((Condition) post).setErrors(err);
                //set err number for lower condition
                if (!isLower) {
                    for (Condition min : bsonAlg.getMinimalPhase(getActivatedPhases(phases.get(post)))) {
                        min.setErrors(min.getErrors() + ((Condition) post).getErrors());
                    }
                }
            }
            if (post instanceof ChannelPlace) {
                ((ChannelPlace) post).setErrors(err);
            }
        }
    }

    private void setSyncErrNum(Collection<TransitionNode> sync, Map<Condition, Collection<Phase>> phases, boolean isLower) {
        int err = 0;

        for (TransitionNode e : sync) {
            if (e.isFaulty()) {
                err++;
            }
            for (MathNode pre : net.getPreset((MathNode) e)) {
                if (pre instanceof Condition) {
                    err += ((Condition) pre).getErrors();
                }
                if (pre instanceof ChannelPlace) {
                    for (Node n : net.getPreset(pre)) {
                        if (!sync.contains(n)) {
                            err += ((ChannelPlace) pre).getErrors();
                        }
                    }
                }
            }
        }

        for (TransitionNode e : sync) {
            for (MathNode post: net.getPostset((MathNode) e)) {
                if (post instanceof Condition) {
                    ((Condition) post).setErrors(err);
                    //set err number for upper conditions
                    if (!isLower) {
                        for (Condition min : bsonAlg.getMinimalPhase(getActivatedPhases(phases.get(post)))) {
                            min.setErrors(min.getErrors() + ((Condition) post).getErrors());
                        }
                    }
                }

                if (post instanceof ChannelPlace) {
                    for (MathNode n : net.getPostset(post)) {
                        if ((n instanceof TransitionNode) && !sync.contains(n)) {
                            ((ChannelPlace) post).setErrors(err);
                        }
                    }
                }
            }
        }
    }

    //Backward error tracing
    public void setRevErrNum(Collection<TransitionNode> fireList, Collection<Path> sync, Map<Condition, Collection<Phase>> phases, boolean isLower) {

        while (!fireList.isEmpty()) {
            Collection<TransitionNode> removeList = new ArrayList<>();

            for (TransitionNode e : fireList) {
                if (!net.getSONConnectionTypes((MathNode) e).contains(Semantics.SYNCLINE)) {
                    //set number from the very first asynchronous event
                    if (getPostAsynEvents(e).isEmpty() || !hasCommonElements(fireList, getPostAsynEvents(e))) {
                        setRevAsynErrNum(e, phases, isLower);
                        fireList.remove(e);
                        break;
                    }
                }

                //set number for the synchronous events with no pre-async-events
                boolean b = false;
                for (Path cycle : sync) {
                    if (cycle.contains(e)) {
                        Collection<TransitionNode> fireList2 = new ArrayList<>();
                        Collection<TransitionNode> eventCycle = new ArrayList<>();

                        fireList2.addAll(fireList);
                        fireList2.removeAll(cycle);
                        boolean hasPostAsyn = false;
                        for (Node n: cycle) {
                            if (n instanceof TransitionNode) {
                                eventCycle.add((TransitionNode) n);
                                if (!getPostAsynEvents((TransitionNode) n).isEmpty()
                                        && hasCommonElements(fireList2, getPostAsynEvents((TransitionNode) n))) {
                                    hasPostAsyn = true;
                                }
                            }
                        }
                        if (!hasPostAsyn) {
                            setRevSyncErrNum(eventCycle, phases, isLower);
                            removeList.addAll(eventCycle);
                            b = true;
                            break;
                        }
                    }
                }
                if (b) {
                    break;
                }
            }
            fireList.removeAll(removeList);
        }
    }

    private void setRevAsynErrNum(TransitionNode e, Map<Condition, Collection<Phase>> phases, boolean isLower) {
        int err = 0;
        if (e.isFaulty()) {
            err++;
        }
        //get err number from lower conditions and channel places
        for (MathNode pre : net.getPreset((MathNode) e)) {
            if (pre instanceof PlaceNode) {
                err += ((PlaceNode) pre).getErrors();
            }
        }

        for (MathNode post: net.getPostset((MathNode) e)) {
            if (post instanceof Condition) {
                ((Condition) post).setErrors(err);
                //set err number for lower condition
                if (!isLower) {
                    for (Condition min : bsonAlg.getMinimalPhase(getActivatedPhases(phases.get(post)))) {
                        min.setErrors(min.getErrors() - ((Condition) post).getErrors());
                    }
                }
                ((Condition) post).setErrors(((Condition) post).getErrors() - err);
            }
            if (post instanceof ChannelPlace) {
                ((ChannelPlace) post).setErrors(((ChannelPlace) post).getErrors() - err);
            }
        }
    }

    private void setRevSyncErrNum(Collection<TransitionNode> sync, Map<Condition, Collection<Phase>> phases, boolean isLower) {
        int err = 0;

        for (TransitionNode e : sync) {
            if (e.isFaulty()) {
                err++;
            }
            for (MathNode pre : net.getPreset((MathNode) e)) {
                if (pre instanceof Condition) {
                    err += ((Condition) pre).getErrors();
                }
                if (pre instanceof ChannelPlace) {
                    for (MathNode n : net.getPreset(pre)) {
                        if ((n instanceof TransitionNode) && !sync.contains(n)) {
                            err += ((ChannelPlace) pre).getErrors();
                        }
                    }
                }
            }
        }

        for (TransitionNode e : sync) {
            for (MathNode post: net.getPostset((MathNode) e)) {
                if (post instanceof Condition) {
                    ((Condition) post).setErrors(err);
                    //set err number for upper conditions
                    if (!isLower) {
                        for (Condition min : bsonAlg.getMinimalPhase(getActivatedPhases(phases.get(post)))) {
                            min.setErrors(min.getErrors() - ((Condition) post).getErrors());
                        }
                    }
                    ((Condition) post).setErrors(((Condition) post).getErrors() - err);
                }

                if (post instanceof ChannelPlace) {
                    for (MathNode n : net.getPostset(post)) {
                        if ((n instanceof TransitionNode) && !sync.contains(n)) {
                            ((ChannelPlace) post).setErrors(((ChannelPlace) post).getErrors() - err);
                        }
                    }
                }
            }
        }
    }

}
