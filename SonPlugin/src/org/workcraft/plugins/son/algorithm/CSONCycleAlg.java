package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.TransitionNode;

import java.util.*;

public class CSONCycleAlg extends ONCycleAlg {

    private final SON net;

    public CSONCycleAlg(SON net) {
        super(net);
        this.net = net;
    }

    /**
     * create Integer Graph for a nodes set
     * Synchronous communication would be treated as an undirected line.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List<Integer>[] createGraph(List<Node> nodes) {
        List<Integer>[] result = new List[nodes.size()];

        LinkedHashMap<Node, Integer> nodeIndex = new LinkedHashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            nodeIndex.put(nodes.get(i), i);
        }

        if (nodes.size() == nodeIndex.size()) {
            for (int i = 0; i < nodes.size(); i++) {
                int index = nodeIndex.get(nodes.get(i));

                if (result[index] == null) {
                    result[index] = new ArrayList<Integer>();
                }
                MathNode node = (MathNode) nodes.get(index);
                for (MathNode post: net.getPostset(node)) {
                    if (nodes.contains(post)) {
                        result[index].add(nodeIndex.get(post));
                        //reverse direction for synchronous connection
                        if (net.getSONConnectionType(node, post) == Semantics.SYNCLINE) {
                            int index2 = nodeIndex.get(post);
                            if (result[index2] == null) {
                                result[index2] = new ArrayList<Integer>();
                            }
                            result[index2].add(index);
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("fail to create graph, input size is not equal to nodeIndex size");
        }

        return result;
    }

    /**
     * get synchronous event cycle (without channel places) for a set of nodes.
     */
    public Collection<Path> syncEventCycleTask(Collection<? extends Node> nodes) {
        HashSet<Node> fliter = new HashSet<>();
        for (Node node : nodes) {
            if ((node instanceof ChannelPlace) || (node instanceof TransitionNode)) {
                fliter.add(node);
            }
        }
        return syncEventCycleFliter(super.cycleTask(fliter));
    }

    private Collection<Path> syncEventCycleFliter(Collection<Path> paths) {
        List<Path> result = new ArrayList<>();
        for (Path path : paths) {
            Path sub = new Path();
            for (Node node : path) {
                if (node instanceof TransitionNode) {
                    sub.add(node);
                }
            }
            result.add(sub);
        }
        return result;
    }

    /**
     * get synchronous event cycle (with channel places) for a set nodes.
     */
    public Collection<Path> syncCycleTask(Collection<? extends Node> nodes) {
        HashSet<Node> fliter = new HashSet<>();
        for (Node node : nodes) {
            if ((node instanceof ChannelPlace) || (node instanceof TransitionNode)) {
                fliter.add(node);
            }
        }
        return super.cycleTask(fliter);
    }

    /**
     *     get a/synchronous cycles
     */
    @Override
    public Collection<Path> cycleTask(Collection<? extends Node> nodes) {
        return cycleFliter(super.cycleTask(nodes));
    }

    /**
     *     Flit a/synchronous cycles
     */
    @Override
    protected Collection<Path> cycleFliter(Collection<Path> paths) {
        List<Path> delList = new ArrayList<>();
        for (Path path : paths) {
            if (!net.getSONConnectionTypes(path).contains(Semantics.PNLINE)) {
                delList.add(path);
            }
            if (!net.getSONConnectionTypes(path).contains(Semantics.SYNCLINE)
                    && !net.getSONConnectionTypes(path).contains(Semantics.ASYNLINE)) {
                delList.add(path);
            }
        }
        paths.removeAll(delList);

        return paths;
    }
}
