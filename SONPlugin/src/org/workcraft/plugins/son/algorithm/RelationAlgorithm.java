package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;

public class RelationAlgorithm {

    protected SON net;

    public RelationAlgorithm(SON net) {
        this.net = net;
    }

    /**
     * check if a given condition has more than one input events
     */
    public boolean hasPostConflictEvents(Node c) {
        if (getPostConflictEvents(c).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public Collection<TransitionNode> getPostConflictEvents(Node c) {
        Collection<TransitionNode> result = new ArrayList<>();

        if (c instanceof Condition) {
            Collection<Node> postset = getPostPNSet(c);
            if (postset.size() > 1) {
                for (Node post : postset) {
                    if (post instanceof TransitionNode) {
                        result.add((TransitionNode) post);
                    }
                }
            }
        }
        return result;
    }

    /**
     * check if a given condition has more than one output events
     */
    public boolean hasPreConflictEvents(Node c) {
        if (getPreConflictEvents(c).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public Collection<TransitionNode> getPreConflictEvents(Node c) {
        Collection<TransitionNode> result = new ArrayList<>();

        if (c instanceof Condition) {
            Collection<Node> preset = getPrePNSet(c);
            if (preset.size() > 1) {
                for (Node pre : preset) {
                    if (pre instanceof TransitionNode) {
                        result.add((TransitionNode) pre);
                    }
                }
            }
        }
        return result;
    }

    /**
     * check if a given node is initial state (condition)
     */
    public boolean isInitial(Node n) {

        if (getPrePNSet(n).size() == 0) {
            return true;
        } 
        return false;
    }

    /**
     * check if a given node is final state (condition)
     */
    public boolean isFinal(Node n) {

        if (getPostPNSet(n).size() == 0) {
            return true;
        } 
        return false;
    }

    /**
     * check if a given set of nodes contains initial states.
     */
    public boolean hasInitial(Collection<? extends Node> nodes) {
        boolean result = false;

        for (Node node : nodes) {
            if (isInitial(node)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * check if a given set of nodes contains final states.
     */
    public boolean hasFinal(Collection<? extends Node> nodes) {
        boolean result = false;

        for (Node node : nodes) {
            if (isFinal(node)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * get all initial states of a given node set
     */
    public Collection<Condition> getONInitial(ONGroup group) {
        ArrayList<Condition> result =  new ArrayList<Condition>();
        for (Condition node : group.getConditions()) {
            if (isInitial(node)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * get all final states of a given node set
     */
    public Collection<Condition> getONFinal(ONGroup group) {
        ArrayList<Condition> result =  new ArrayList<Condition>();
        for (Condition node : group.getConditions()) {
            if (isFinal(node)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * get all connected channel places for a set of groups
     */
    public Collection<ChannelPlace> getRelatedChannelPlace(Collection<ONGroup> groups) {
        HashSet<ChannelPlace> result = new HashSet<>();

        for (ChannelPlace cPlace : net.getChannelPlaces()) {
            for (ONGroup group : groups) {
                for (Node node : net.getPostset(cPlace)) {
                    if (group.contains(node)) {
                        result.add(cPlace);
                    }
                }
                for (Node node : net.getPreset(cPlace)) {
                    if (group.contains(node)) {
                        result.add(cPlace);
                    }
                }
            }
        }

        return result;
    }

    /**
     * get all PN-based(petri net) pre-conditions for a given condition
     */
    public Collection<Condition> getPrePNCondition(Condition c) {
        Collection<Condition> result = new ArrayList<>();
        for (Node pre : net.getPreset(c)) {
            if (pre instanceof TransitionNode) {
                if (net.getSONConnectionType(c, pre) == Semantics.PNLINE) {
                    for (Node n2 : net.getPreset(pre)) {
                        if ((n2 instanceof Condition) && net.getSONConnectionType(pre, n2) == Semantics.PNLINE) {
                            result.add((Condition) n2);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * get all PN-based post-conditions for a given condition
     */
    public Collection<Condition> getPostPNCondition(Condition c) {
        Collection<Condition> result = new ArrayList<>();
        for (Node post : net.getPostset(c)) {
            if (post instanceof TransitionNode) {
                if (net.getSONConnectionType(c, post) == Semantics.PNLINE) {
                    for (Node n2 : net.getPostset(post)) {
                        if ((n2 instanceof Condition) && net.getSONConnectionType(post, n2) == Semantics.PNLINE) {
                            result.add((Condition) n2);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * get all asynchronous (Communication-SON) pre-events for a given event node
     */
    public Collection<TransitionNode> getPreAsynEvents(TransitionNode e) {
        Collection<TransitionNode> result = new ArrayList<>();
        for (Node pre : net.getPreset(e)) {
            if ((pre instanceof ChannelPlace) && net.getSONConnectionType(pre, e) == Semantics.ASYNLINE) {

                Iterator<Node> it = net.getPreset(pre).iterator();

                while (it.hasNext()) {
                    result.add((TransitionNode) it.next());
                }
            }
        }
        return result;
    }

    /**
     * get all asynchronous (Communication-SON) post-events for a given event node
     */
    public Collection<TransitionNode> getPostAsynEvents(TransitionNode e) {
        Collection<TransitionNode> result = new ArrayList<>();
        for (Node post : net.getPostset(e)) {
            if ((post instanceof ChannelPlace) && net.getSONConnectionType(post, e) == Semantics.ASYNLINE) {

                Iterator<Node> it = net.getPostset(post).iterator();

                while (it.hasNext()) {
                    result.add((TransitionNode) it.next());
                }
            }
        }
        return result;
    }

    /**
     * get all asynchronous and synchronous (Communication-SON) pre-event for a given event or collapsed block
     */
    public Collection<TransitionNode> getPreASynEvents(TransitionNode e) {
        Collection<TransitionNode> result = new ArrayList<>();

        for (Node pre : net.getPreset(e)) {
            if (pre instanceof ChannelPlace) {

                Iterator<Node> it = net.getPreset(pre).iterator();

                while (it.hasNext()) {
                    result.add((TransitionNode) it.next());
                }
            }
        }
        for (Node post : net.getPostset(e)) {
            if ((post instanceof ChannelPlace) && net.getSONConnectionType(post, e) == Semantics.SYNCLINE) {

                Iterator<Node> it = net.getPostset(post).iterator();

                while (it.hasNext()) {
                    result.add((TransitionNode) it.next());
                }
            }
        }

        return result;
    }

    /**
     * get all asynchronous and synchronous(Communication-SON) post-event for a given event or block
     */
    public Collection<TransitionNode> getPostASynEvents(TransitionNode node) {
        Collection<TransitionNode> result = new ArrayList<>();

        for (Node post : net.getPostset(node)) {
            if (post instanceof ChannelPlace) {

                Iterator<Node> it = net.getPostset(post).iterator();

                while (it.hasNext()) {
                    result.add((TransitionNode) it.next());
                }
            }
        }
        for (Node pre : net.getPreset(node)) {
            if (pre instanceof ChannelPlace && net.getSONConnectionType(pre, node) == Semantics.SYNCLINE) {

                Iterator<Node> it = net.getPreset(pre).iterator();

                while (it.hasNext()) {
                    result.add((TransitionNode) it.next());
                }
            }
        }

        return result;
    }

    /**
     * get all PRE-conditions (PN and CSON-based) for a given event or block.
     */
    public Collection<Condition> getPREset(TransitionNode e) {
        Collection<Condition> result = new ArrayList<>();
        for (Node n : net.getPreset(e)) {
            if (n instanceof Condition) {
                result.add((Condition) n);
            }
            if (n instanceof ChannelPlace) {

                Iterator<Node> it = net.getPreset(n).iterator();

                while (it.hasNext()) {
                    for (Node preCondition : net.getPreset((TransitionNode) it.next())) {
                        if (preCondition instanceof Condition) {
                            result.add((Condition) preCondition);
                        }
                    }
                }
            }
        }

        for (Node n : net.getPostset(e)) {
            if (n instanceof ChannelPlace && net.getSONConnectionType(e, n) == Semantics.SYNCLINE) {

                Iterator<Node> it = net.getPostset(n).iterator();

                while (it.hasNext()) {
                    for (Node preCondition : net.getPreset((TransitionNode) it.next())) {
                        if (preCondition instanceof Condition) {
                            result.add((Condition) preCondition);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * get all POST-conditions (PN and CSON-based) for a given event or block.
     */
    public Collection<Condition> getPOSTset(TransitionNode e) {
        Collection<Condition> result = new ArrayList<>();

        for (Node n : net.getPostset(e)) {
            if (n instanceof Condition) {
                result.add((Condition) n);
            }
            if (n instanceof ChannelPlace) {

                Iterator<Node> it = net.getPostset(n).iterator();

                while (it.hasNext()) {
                    for (Node postCondition : net.getPostset((TransitionNode) it.next())) {
                        if (postCondition instanceof Condition) {
                            result.add((Condition) postCondition);
                        }
                    }
                }
            }
        }

        for (Node n : net.getPreset(e)) {
            if ((n instanceof ChannelPlace) && net.getSONConnectionType(e, n) == Semantics.SYNCLINE) {

                Iterator<Node> it = net.getPreset(n).iterator();

                while (it.hasNext()) {
                    for (Node postCondition : net.getPostset(it.next())) {
                        if (postCondition instanceof Condition) {
                            result.add((Condition) postCondition);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * get all PN-based preset for a given node.
     */
    public Collection<Node> getPrePNSet(Node node) {
        Collection<Node> result = new ArrayList<>();
        for (Node n : net.getPreset(node)) {
            if (net.getSONConnectionType(node, n) == Semantics.PNLINE) {
                result.add(n);
            }
        }
        return result;
    }

    /**
     * get all PN-based postset for a given node.
     */
    public Collection<Node> getPostPNSet(Node node) {
        Collection<Node> result = new ArrayList<>();
        for (Node n : net.getPostset(node)) {
            if (net.getSONConnectionType(node, n) == Semantics.PNLINE) {
                result.add(n);
            }
        }
        return result;
    }

    /**
     * get all Bhv-based postset for a given condition.
     */
    public Collection<Condition> getPostBhvSet(Condition c) {
        Collection<Condition> result = new ArrayList<>();
        for (Node n : net.getPostset(c)) {
            if (net.getSONConnectionType(c, n) == Semantics.BHVLINE) {
                result.add((Condition) n);
            }
        }
        return result;
    }

    /**
     * get all Bhv-based preset for a given condition.
     */
    public Collection<Condition> getPreBhvSet(Condition c) {
        Collection<Condition> result = new ArrayList<>();
        for (Node n : net.getPreset(c)) {
            if (net.getSONConnectionType(c, n) == Semantics.BHVLINE) {
                result.add((Condition) n);
            }
        }
        return result;
    }

    public Collection<Node> getPreset(Collection<Node> nodes) {
        Collection<Node> result = new HashSet<>();
        for (Node node : nodes) {
            for (Node pre : net.getPreset(node)) {
                if (!nodes.contains(pre)) {
                    result.add(pre);
                }
            }
        }

        return result;
    }

    public Collection<Node> getCommonElements(Collection<? extends Node> set1, Collection<? extends Node> set2) {
        Collection<Node> result = new HashSet<>();
        for (Node node : set1) {
            if (set2.contains(node)) {
                result.add(node);
            }
        }

        for (Node node : set2) {
            if (set1.contains(node)) {
                result.add(node);
            }
        }
        return result;
    }
}
