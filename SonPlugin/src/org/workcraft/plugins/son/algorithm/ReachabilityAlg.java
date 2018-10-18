package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.util.Before;

public class ReachabilityAlg extends RelationAlgorithm {

    private static final Collection<Node> predecessors = new HashSet<>();
    private final SON net;

    public ReachabilityAlg(SON net) {
        super(net);
        this.net = net;
    }

    //get path between a given initial node and a set of final nodes. (recursion)
//    private void dfs(LinkedList<Node> visited, Collection<Node> v, Before before) {
//        LinkedList<Node> post = getCausalPreset(visited.getLast(), before);
//        if (v.contains(visited.getLast())) {
//            pathResult.addAll(visited);
//        }
//        // examine post nodes
//        for (Node node : post) {
//            if (visited.contains(node)) {
//                continue;
//            }
//            if (v.contains(node)) {
//                visited.add(node);
//                pathResult.addAll(visited);
//                visited.removeLast();
//                break;
//            }
//        }
//        // in depth-first, recursion needs to come after visiting post nodes
//        for (Node node : post) {
//            if (visited.contains(node) || node.equals(v)) {
//                continue;
//            }
//            visited.addLast(node);
//            dfs(visited, v, before);
//            visited.removeLast();
//        }
//    }

    private void causalPredecessors(LinkedList<Node> visited, Node n, Before before) {
        predecessors.add(n);
        visited.add(n);

        for (Node n2 : getCausalPreset(n, before)) {
            if (!visited.contains(n2)) {
                causalPredecessors(visited, n2, before);
            }
        }
    }

    public Collection<Node> getCausalPredecessors(Node s) {
        predecessors.clear();
        LinkedList<Node> visited = new LinkedList<>();
        BSONAlg bsonAlg = new BSONAlg(net);
        Before before = bsonAlg.getBeforeList();
        visited.add(s);
        //dfs(visited, v, before);
        causalPredecessors(visited, s, before);
        return predecessors;
    }

    private LinkedList<Node> getCausalPreset(Node n, Before before) {
        LinkedList<Node> result = new LinkedList<>();

        if (isInitial(n) && (n instanceof Condition)) {
            result.addAll(getPostBhvSet((Condition) n));
        }

        for (TransitionNode[] pre : before) {
            if (pre[1] == n) {
                result.add(pre[0]);
            }
        }

        result.addAll(getPrePNSet(n));

        if (isInitial(n) && (n instanceof Condition)) {
            result.addAll(getPostBhvSet((Condition) n));
        } else if (n instanceof TransitionNode) {
            for (SONConnection con : net.getSONConnections((MathNode) n)) {
                if (con.getSemantics() == Semantics.SYNCLINE) {
                    if (con.getFirst() == n) {
                        result.add(con.getSecond());
                    } else {
                        result.add(con.getFirst());
                    }
                } else if (con.getSemantics() == Semantics.ASYNLINE && con.getSecond() == n) {
                    result.add(con.getFirst());
                }
            }
        } else if (n instanceof ChannelPlace) {
            Node input = net.getPreset((MathNode) n).iterator().next();
            result.add(input);
            Collection<Semantics> semantics = net.getSONConnectionTypes((MathNode) n);
            if (semantics.iterator().next() == Semantics.SYNCLINE) {
                Node output = net.getPostset((MathNode) n).iterator().next();
                result.add(output);
            }
        }

        return result;
    }
}
