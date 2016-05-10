package org.workcraft.dom.visual;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Hierarchy;

public class SelectionHelper {

    static public Collection<Node> getOrderedCurrentLevelSelection(VisualModel model) {
        HashSet<Node> result = new HashSet<>();
        Collection<Node> selection = model.getSelection();
        Container currentLevel = model.getCurrentLevel();
        for (Node node : currentLevel.getChildren()) {
            if (selection.contains(node)) {
                result.add(node);
            }
        }
        return result;
    }

    static public Collection<Node> getRecursivelyIncludedNodes(Collection<Node> nodes) {
        HashSet<Node> result = new HashSet<>();
        for (Node node : nodes) {
            if (node instanceof VisualNode) {
                result.add(node);
                result.addAll(Hierarchy.getDescendantsOfType(node, VisualNode.class));
            }
        }
        return result;
    }

    static public Collection<VisualConnection> getCurrentLevelConnections(VisualModel model) {
        return Hierarchy.getChildrenOfType(model.getCurrentLevel(), VisualConnection.class);
    }

    static public Collection<Node> getGroupableCurrentLevelSelection(VisualModel model) {
        HashSet<Node> result = new HashSet<>();
        Collection<Node> currentLevelSelection = getOrderedCurrentLevelSelection(model);
        for (Node node : currentLevelSelection) {
            if (model.isGroupable(node) && !(node instanceof VisualConnection)) {
                result.add(node);
            }
        }
        Collection<VisualConnection> currentLevelConnections = getCurrentLevelConnections(model);
        Collection<VisualConnection> includedConnections = getIncludedConnections(model.getSelection(), currentLevelConnections);
        result.addAll(includedConnections);
        return result;
    }

    static public Collection<VisualConnection> getIncludedConnections(Collection<Node> nodes, Collection<VisualConnection> connections) {
        Collection<VisualConnection> result = new HashSet<>();
        Collection<Node> recursiveNodes = getRecursivelyIncludedNodes(nodes);
        for (VisualConnection connection : connections) {
            VisualNode first = connection.getFirst();
            VisualNode second = connection.getSecond();
            if (recursiveNodes.contains(first) && recursiveNodes.contains(second)) {
                result.add(connection);
            }
        }
        return result;
    }

    static public void selectByReferencedComponents(VisualModel model, HashSet<MathNode> nodes) {
        model.selectNone();
        for (VisualComponent component: Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class)) {
            MathNode node = component.getReferencedComponent();
            if (nodes.contains(node)) {
                model.addToSelection(component);
            }
        }
    }

}
