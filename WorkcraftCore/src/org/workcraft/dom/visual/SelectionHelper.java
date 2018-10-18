package org.workcraft.dom.visual;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.util.Hierarchy;

import java.util.Collection;
import java.util.HashSet;

public class SelectionHelper {

    public static Collection<VisualNode> getOrderedCurrentLevelSelection(VisualModel model) {
        HashSet<VisualNode> result = new HashSet<>();
        Collection<? extends VisualNode> selection = model.getSelection();
        Container currentLevel = model.getCurrentLevel();
        for (Node node : currentLevel.getChildren()) {
            if ((node instanceof VisualNode) && selection.contains(node)) {
                result.add((VisualNode) node);
            }
        }
        return result;
    }

    public static Collection<VisualNode> getRecursivelyIncludedNodes(Collection<? extends VisualNode> nodes) {
        HashSet<VisualNode> result = new HashSet<>();
        for (VisualNode node : nodes) {
            result.add(node);
            result.addAll(Hierarchy.getDescendantsOfType(node, VisualNode.class));
        }
        return result;
    }

    public static Collection<VisualConnection> getCurrentLevelConnections(VisualModel model) {
        return Hierarchy.getChildrenOfType(model.getCurrentLevel(), VisualConnection.class);
    }

    public static Collection<VisualNode> getGroupableCurrentLevelSelection(VisualModel model) {
        HashSet<VisualNode> result = new HashSet<>();
        Collection<VisualNode> currentLevelSelection = getOrderedCurrentLevelSelection(model);
        for (VisualNode node : currentLevelSelection) {
            if (model.isGroupable(node) && !(node instanceof VisualConnection)) {
                result.add(node);
            }
        }
        Collection<VisualConnection> currentLevelConnections = getCurrentLevelConnections(model);
        Collection<VisualConnection> includedConnections = getIncludedConnections(model.getSelection(), currentLevelConnections);
        result.addAll(includedConnections);
        return result;
    }

    public static Collection<VisualConnection> getIncludedConnections(Collection<? extends VisualNode> nodes,
            Collection<? extends VisualConnection> connections) {
        Collection<VisualConnection> result = new HashSet<>();
        Collection<VisualNode> recursiveNodes = getRecursivelyIncludedNodes(nodes);
        for (VisualConnection connection : connections) {
            VisualNode first = connection.getFirst();
            VisualNode second = connection.getSecond();
            if (recursiveNodes.contains(first) && recursiveNodes.contains(second)) {
                result.add(connection);
            }
        }
        return result;
    }

    public static void selectByReferencedComponents(VisualModel model, HashSet<? extends MathNode> nodes) {
        model.selectNone();
        for (VisualComponent component: Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class)) {
            MathNode node = component.getReferencedComponent();
            if (nodes.contains(node)) {
                model.addToSelection(component);
            }
        }
    }

}
