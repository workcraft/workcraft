package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.util.Phase;

public class BSONCycleAlg extends ONCycleAlg {

    private final SON net;
    protected BSONAlg bsonAlg;
    private final Map<Condition, Collection<Phase>> phases;

    public BSONCycleAlg(SON net, Map<Condition, Collection<Phase>> phases) {
        super(net);
        this.net = net;
        this.phases = phases;
        bsonAlg = new BSONAlg(net);
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
                    result[index] = new ArrayList<>();
                }

                MathNode node = (MathNode) nodes.get(index);
                for (MathNode post: net.getPostset(node)) {
                    if (nodes.contains(post) && net.getSONConnectionType(node, post) != Semantics.BHVLINE) {
                        result[index].add(nodeIndex.get(post));

                        //reverse direction for synchronous connection
                        if (net.getSONConnectionType(node, post) == Semantics.SYNCLINE) {
                            int index2 = nodeIndex.get(post);
                            if (result[index2] == null) {
                                result[index2] = new ArrayList<>();
                            }
                            result[index2].add(index);
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("fail to create graph, input size is not equal to nodeIndex size");
        }

        //get upper-level transition nodes.
        Collection<ONGroup> upperGroups = bsonAlg.getUpperGroups(net.getGroups());
        Collection<TransitionNode> upperT = new ArrayList<>();
        for (ONGroup group : upperGroups) {
            upperT.addAll(group.getTransitionNodes());
        }

        for (int i = 0; i < nodes.size(); i++) {
            //add before relation
            Node n = nodes.get(i);
            if (upperT.contains(n)) {
                for (TransitionNode[] v : bsonAlg.before((TransitionNode) n, phases)) {
                    TransitionNode v0 = v[0];
                    TransitionNode v1 = v[1];
                    int index = nodeIndex.get(v0);
                    if (result[index] == null) {
                        result[index] = new ArrayList<>();
                    }
                    result[index].add(nodeIndex.get(v1));
                }
            }
        }
        return result;
    }

    @Override
    public Collection<Path> cycleTask(Collection<? extends Node> nodes) {
        //remove all paths which do not involve before(e) relation.
        return cycleFliter(super.cycleTask(nodes));
    }

    @Override
    protected Collection<Path> cycleFliter(Collection<Path> paths) {
        List<Path> delList = new ArrayList<>();

        for (Path cycle : paths) {
            int upper = 0;
            int lower = 0;

            for (Node n : cycle) {
                if (!(n instanceof ChannelPlace)) {
                    if (bsonAlg.isUpperNode(n)) {
                        upper++;
                    } else {
                        lower++;
                    }
                }
            }
            //all cycle nodes are in the same level
            if (upper == 0 || lower == 0) {
                delList.add(cycle);
            }
        }
        paths.removeAll(delList);
        return paths;
    }

}
