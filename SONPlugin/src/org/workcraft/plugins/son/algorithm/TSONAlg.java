package org.workcraft.plugins.son.algorithm;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.TransitionNode;

public class TSONAlg extends RelationAlgorithm {

    private SON net;
    private RelationAlgorithm relation;

    public TSONAlg(SON net) {
        super(net);
        this.net = net;
        relation = new RelationAlgorithm(net);
    }

    /**
     * get all inputs of a given block without concerning connection types
     */
    public Collection<Node> getBlockInputs(Block block) {
        Collection<Node> result = new HashSet<Node>();
        Collection<Node> components = block.getComponents();

        for (Node node : components) {
            for (Node pre : net.getPreset(node)) {
                if (!components.contains(pre))
                    result.add(pre);
            }
            for (Node post : net.getPostset(node)) {
                if (!components.contains(post) && net.getSONConnectionType(node, post) == Semantics.SYNCLINE)
                    result.add(post);
            }
        }
        result.addAll(net.getPreset(block));
        return result;
    }

    /**
     * get all outputs of a given block without concerning connection types
     */
    public Collection<Node> getBlockOutputs(Block block) {
        Collection<Node> result = new HashSet<Node>();
        Collection<Node> components = block.getComponents();

        for (Node node: components) {
            for (Node post : net.getPostset(node)) {
                if (!components.contains(post))
                    result.add(post);
            }
            for (Node pre : net.getPreset(node)) {
                if (!components.contains(pre) && net.getSONConnectionType(node, pre) == Semantics.SYNCLINE)
                    result.add(pre);
            }
        }
        result.addAll(net.getPostset(block));
        return result;
    }

    /**
     * get petri net-based inputs of a given block
     */
    public Collection<Node> getBlockPNInputs(Block block) {
        Collection<Node> result = new HashSet<Node>();
        Collection<Node> components = block.getComponents();

        for (Node node : components) {
            for (Node pre : net.getPreset(node)) {
                if (!components.contains(pre) && net.getSONConnectionType(node, pre) == Semantics.PNLINE)
                    result.add(pre);
            }
        }
        for (Node pre : net.getPreset(block))
            if (net.getSONConnectionType(block, pre) == Semantics.PNLINE)
                result.add(pre);

        return result;
    }

    /**
     * get petri net-based outputs of a given block
     */
    public Collection<Node> getBlockPNOutputs(Block block) {
        Collection<Node> result = new HashSet<Node>();
        Collection<Node> components = block.getComponents();

        for (Node node : components) {
            for (Node post : net.getPostset(node)) {
                if (!components.contains(post) && net.getSONConnectionType(node, post) == Semantics.PNLINE)
                    result.add(post);
            }
        }
        for (Node post : net.getPostset(block))
            if (net.getSONConnectionType(block, post) == Semantics.PNLINE)
                result.add(post);

        return result;
    }

    /**
     * get cson-based inputs of a given block
     */
    public Collection<Node> getBlockASynInputs(Block block) {
        Collection<Node> result = new HashSet<Node>();
        Collection<Node> components = block.getComponents();

        for (Node node : components) {
            if (node instanceof TransitionNode)
                for (Node pre : this.getPreASynEvents((TransitionNode) node)) {
                    if (!components.contains(pre) && pre != null)
                        result.add(pre);
                }
        }
        return result;
    }

    /**
     * get cson-based outputs of a given block
     */
    public Collection<Node> getBlockASynOutputs(Block block) {
        Collection<Node> result = new HashSet<Node>();
        Collection<Node> components = block.getComponents();

        for (Node node : components) {
            if (node instanceof TransitionNode)
                for (Node post : this.getPostASynEvents((TransitionNode) node)) {
                    if (!components.contains(post) && post != null)
                        result.add(post);
                }
        }
        return result;
    }

    /**
     * check if a given input causally precede all outputs
     */
    public boolean isCausallyPrecede(Node input, Collection<Node> outputs) {
        for (Node post : relation.getPostPNSet(input)) {
            if (outputs.contains(post))
                outputs.remove(post);
            else
                isCausallyPrecede(post, outputs);
        }
        if (outputs.isEmpty())
            return true;
        else
            return false;
    }

}
