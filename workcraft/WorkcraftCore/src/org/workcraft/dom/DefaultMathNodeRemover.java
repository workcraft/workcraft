package org.workcraft.dom;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;

public class DefaultMathNodeRemover extends HierarchySupervisor {

    private final HashMap<MathNode, HashSet<Dependent>> referenceTracker = new HashMap<>();

    private void addReference(MathNode mathNode, Dependent dependentNode) {
        if (mathNode != null) {
            HashSet<Dependent> refs = referenceTracker.get(mathNode);
            if (refs == null) {
                refs = new HashSet<>();
                referenceTracker.put(mathNode, refs);
            }
            refs.add(dependentNode);
        }
    }

    private void removeReference(MathNode mathNode, Dependent dependentNode) {
        if (mathNode != null) {
            HashSet<Dependent> refs = referenceTracker.get(mathNode);
            if (refs != null) {
                refs.remove(dependentNode);
            }
            if ((refs == null) || refs.isEmpty()) {
                referenceTracker.remove(mathNode);
                Node parent = mathNode.getParent();
                if (parent instanceof Container) {
                    ((Container) parent).remove(mathNode);
                }
            }
        }
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletedEvent) {
            for (Node node : e.getAffectedNodes()) {
                nodeRemoved(node);
            }
        }
        if (e instanceof NodesAddedEvent) {
            for (Node node : e.getAffectedNodes()) {
                nodeAdded(node);
            }
        }
    }

    private void nodeAdded(Node node) {
        if (node instanceof Dependent dependentNode) {
            for (MathNode mathNode : dependentNode.getMathReferences()) {
                addReference(mathNode, dependentNode);
            }
        }
        if (node != null) {
            for (Node childNode : node.getChildren()) {
                nodeAdded(childNode);
            }
        }
    }

    private void nodeRemoved(Node node) {
        if (node instanceof Dependent dependentNode) {
            for (MathNode mathNode : dependentNode.getMathReferences()) {
                removeReference(mathNode, dependentNode);
            }
        }
        if (node != null) {
            for (Node childNode : node.getChildren()) {
                nodeRemoved(childNode);
            }
        }
    }

}
