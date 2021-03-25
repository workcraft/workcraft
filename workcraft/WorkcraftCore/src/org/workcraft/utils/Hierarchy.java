package org.workcraft.utils;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.NodeHelper;
import org.workcraft.types.Func;
import org.workcraft.workspace.ModelEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Hierarchy {

    public static <T extends Node> Collection<T> filterNodesByType(Collection<Node> nodes, final Class<T> type) {
        LinkedList<T> result = new LinkedList<>();
        for (Node node : nodes) {
            if (type.isInstance(node)) {
                result.add(type.cast(node));
            }
        }
        return result;
    }

    public static Node[] getPath(Node node) {
        Node n = node;
        int i = 0;
        while (n != null) {
            i++;
            n = n.getParent();
        }
        Node[] result = new Node[i];
        n = node;
        while (n != null) {
            result[--i] = n;
            n = n.getParent();
        }
        return result;
    }

    public static Node getTopParent(Node node) {
        Node top = node;
        while (top.getParent() != null) top = top.getParent();
        return top;
    }

    public static Node getCommonParent(Node... nodes) {
        ArrayList<Node[]> paths = new ArrayList<>(nodes.length);
        int minPathLength = -1;
        for (Node node : nodes) {
            final Node[] path = getPath(node);
            if ((minPathLength < 0) || (minPathLength > path.length)) {
                minPathLength = path.length;
            }
            paths.add(path);
        }
        Node result = null;
        for (int i = 0; i < minPathLength; i++) {
            Node node = paths.get(0)[i];
            boolean good = true;
            for (Node[] path : paths) {
                if (path[i] != node) {
                    good = false;
                    break;
                }
            }

            if (good) {
                result = node;
            } else {
                break;
            }
        }
        return result;
    }

    public static Node getRoot(Node node) {
        Node root = null;
        Node parent = node;
        do {
            parent = parent.getParent();
            if (parent != null) {
                root = parent;
            }
        } while (parent != null);
        return root;
    }

    public static boolean isDescendant(Node descendant, Node parent) {
        Node node = descendant;
        while (node != parent) {
            if (node == null) {
                return false;
            }
            node = node.getParent();
        }
        return true;
    }

    public static Container getNearestContainer(Node... node) {
        Node parent = getCommonParent(node);
        return getNearestAncestor(parent, Container.class);
    }

    public static Container getNearestContainer(Collection<? extends Node> nodes) {
        return getNearestContainer(nodes.toArray(new Node[0]));
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T getNearestAncestor(Node node, final Class<T> type) {
        return (T) getNearestAncestor(node, type::isInstance);
    }

    public static Node getNearestAncestor(Node node, Func<Node, Boolean> filter) {
        Node parent = node;
        while (parent != null) {
            if (filter.eval(parent)) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public static <T> Collection<T> getChildrenOfType(Node node, Class<T> type) {
        return NodeHelper.filterByType(node.getChildren(), type);
    }

    public static <T> Collection<T> getChildrenOfType(Node node, Class<T> type, Func<T, Boolean> filter) {
        return NodeHelper.filterByType(node.getChildren(), type, filter);
    }

    public static <T> Collection<T> getDescendantsOfType(Node node, Class<T> type) {
        ArrayList<T> result = new ArrayList<>();
        for (Node n : node.getChildren()) {
            result.addAll(getDescendantsOfType(n, type));
        }
        result.addAll(getChildrenOfType(node, type));
        return result;
    }

    public static <T> Collection<T> getDescendantsOfType(Node node, Class<T> type, Func<T, Boolean> filter) {
        ArrayList<T> result = new ArrayList<>();
        for (Node n : node.getChildren()) {
            result.addAll(getDescendantsOfType(n, type, filter));
        }
        result.addAll(getChildrenOfType(node, type, filter));
        return result;
    }

    public static boolean isHierarchical(ModelEntry me) {
        MathModel mm = me.getMathModel();
        Collection<PageNode> pageNodes = Hierarchy.getChildrenOfType(mm.getRoot(), PageNode.class);
        return !pageNodes.isEmpty();
    }

}
