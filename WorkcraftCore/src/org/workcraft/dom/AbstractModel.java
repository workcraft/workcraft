package org.workcraft.dom;

import org.workcraft.annotations.Annotations;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.util.Func;

import java.util.*;

/**
 * A base class for all interpreted graph models.
 */
public abstract class AbstractModel<N extends Node, C extends Connection>  implements Model<N, C> {

    private final Container root;
    private final ReferenceManager mgr;
    private final NodeContextTracker<N, C> nodeContextTracker = new NodeContextTracker<>();
    public final boolean generatedRoot;

    private String title = "";

    public AbstractModel(Container root) {
        this(root, null);
    }

    public AbstractModel(Container root, ReferenceManager man) {
        if (root != null) {
            this.root = root;
            this.generatedRoot = false;
        } else {
            this.root = createDefaultRoot();
            this.generatedRoot = true;
        }
        if (man != null) {
            this.mgr = man;
        } else {
            this.mgr = createDefaultReferenceManager();
        }
        this.nodeContextTracker.attach(getRoot());
        this.mgr.attach(getRoot());
    }

    @Override
    public void add(Node node) {
        root.add(node);
    }

    @Override
    public void remove(Node node) {
        if (node.getParent() instanceof Container) {
            ((Container) node.getParent()).remove(node);
        } else {
            throw new RuntimeException("Cannot remove a child node from a node that is not a Container (or null).");
        }
    }

    @Override
    public void remove(Collection<Node> nodes) {
        LinkedList<Node> toRemove = new LinkedList<>(nodes);
        for (Node node : toRemove) {
            // some nodes may be removed as a result of removing other nodes in the list,
            // e.g. hanging connections so need to check
            if (node.getParent() != null) {
                remove(node);
            }
        }
    }

    @Override
    public String getDisplayName() {
        return Annotations.getDisplayName(getClass());
    }

    @Override
    public String getShortName() {
        return Annotations.getShortName(getClass());
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String value) {
        title = value;
    }

    @Override
    public final Container getRoot() {
        return root;
    }

    @Override
    public Set<N> getPostset(N node) {
        return nodeContextTracker.getPostset(node);
    }

    @Override
    public Set<N> getPreset(N node) {
        return nodeContextTracker.getPreset(node);
    }

    @Override
    public Set<C> getConnections(N node) {
        return nodeContextTracker.getConnections(node);
    }

    @Override
    public boolean hasConnection(N first, N second) {
        return nodeContextTracker.hasConnection(first, second);
    }

    @Override
    public C getConnection(N first, N second) {
        return nodeContextTracker.getConnection(first, second);
    }

    @Override
    public <T> Set<T> getPreset(N node, Class<T> type) {
        Set<T> result = new HashSet<>();
        for (N pred: getPreset(node)) {
            try {
                result.add(type.cast(pred));
            } catch (ClassCastException e) {
            }
        }
        return result;
    }

    @Override
    public <T> Set<T> getPostset(N node, Class<T> type) {
        Set<T> result = new HashSet<>();
        for (Node pred: getPostset(node)) {
            try {
                result.add(type.cast(pred));
            } catch (ClassCastException e) {
            }
        }
        return result;
    }

    @Override
    public <T> Set<T> getPreset(N node, Class<T> type, Func<N, Boolean> through) {
        Set<T> result = new HashSet<>();
        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            N cur = queue.remove();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (N pred : getPreset(cur)) {
                try {
                    result.add(type.cast(pred));
                } catch (ClassCastException e) {
                    if (through.eval(pred)) {
                        queue.add(pred);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T> Set<T> getPostset(N node, Class<T> type, Func<N, Boolean> through) {
        Set<T> result = new HashSet<>();
        Set<N> visited = new HashSet<>();
        Queue<N> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            N cur = queue.remove();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (N succ: getPostset(cur)) {
                try {
                    result.add(type.cast(succ));
                } catch (ClassCastException e) {
                    if (through.eval(succ)) {
                        queue.add(succ);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public N getNodeByReference(String reference) {
        return getNodeByReference(null, reference);
    }

    @Override
    public String getNodeReference(Node node) {
        return getNodeReference(null, node);
    }

    @Override
    public N getNodeByReference(NamespaceProvider provider, String reference) {
        return (N) mgr.getNodeByReference(provider, reference);
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        return mgr.getNodeReference(provider, node);
    }

    @Override
    public ReferenceManager getReferenceManager() {
        return mgr;
    }

    @Override
    public String getName(Node node) {
        if (mgr instanceof HierarchyReferenceManager) {
            return ((HierarchyReferenceManager) mgr).getName(node);
        }
        return mgr.getNodeReference(null, node);
    }

    @Override
    public void setName(Node node, String name) {
        if (mgr instanceof HierarchyReferenceManager) {
            ((HierarchyReferenceManager) mgr).setName(node, name);
        }
    }

}
