package org.workcraft.dom.visual;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

    private static Collection<VisualNode> getRecursivelyIncludedNodes(Collection<? extends VisualNode> nodes) {
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
        Collection<VisualNode> recursivelyIncludedNodes = getRecursivelyIncludedNodes(currentLevelSelection);
        // Collect groupable nodes, no connections yet (replicas are only added if their masters are selected)
        for (VisualNode node : currentLevelSelection) {
            if (node instanceof VisualConnection) continue;
            boolean b = true;
            if (node instanceof VisualReplica replica) {
                // Only add proxies whose master is also (transitively) selected
                VisualComponent master = replica.getMaster();
                b = recursivelyIncludedNodes.contains(master);
            }
            if (b && model.isGroupable(node)) {
                result.add(node);
            }
        }
        Collection<VisualConnection> currentLevelConnections = getCurrentLevelConnections(model);
        Collection<VisualConnection> includedConnections = getIncludedConnections(result, currentLevelConnections);
        result.addAll(includedConnections);
        return result;
    }

    public static Collection<VisualConnection> getIncludedConnections(Collection<? extends VisualNode> nodes,
            Collection<? extends VisualConnection> connections) {

        Collection<VisualConnection> result = new HashSet<>();
        Collection<VisualNode> recursivelyIncludedNodes = getRecursivelyIncludedNodes(nodes);
        for (VisualConnection connection : connections) {
            VisualNode first = connection.getFirst();
            VisualNode second = connection.getSecond();
            if (recursivelyIncludedNodes.contains(first) && recursivelyIncludedNodes.contains(second)) {
                result.add(connection);
            }
        }
        return result;
    }

    public static void selectByReferencedComponents(VisualModel model, Set<? extends MathNode> nodes) {
        model.selectNone();
        for (VisualComponent component : Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class)) {
            MathNode node = component.getReferencedComponent();
            if (nodes.contains(node)) {
                model.addToSelection(component);
            }
        }
    }

    public static void selectByReferencedComponentsOrConnections(VisualModel model, Set<? extends MathNode> mathNodes) {
        model.selectNone();
        for (VisualComponent visualComponent : Hierarchy.getDescendantsOfType(model.getRoot(), VisualComponent.class)) {
            if (mathNodes.contains(visualComponent.getReferencedComponent())) {
                model.addToSelection(visualComponent);
            }
        }
        for (VisualConnection visualConnection : Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class)) {
            if (mathNodes.contains(visualConnection.getReferencedConnection())) {
                model.addToSelection(visualConnection);
            }
        }
    }

    public static <T extends VisualNode> Collection<T> getSelectedNodes(VisualModel model, Class<T> type) {
        Collection<T> result = Hierarchy.getDescendantsOfType(model.getRoot(), type);
        result.retainAll(model.getSelection());
        return result;
    }

}
