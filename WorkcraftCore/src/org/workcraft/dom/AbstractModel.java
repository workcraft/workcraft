package org.workcraft.dom;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.DefaultReferenceManager;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.util.Func;

/**
 * A base class for all interpreted graph models.
 */
public abstract class AbstractModel implements Model {
    private Container root;
    private ReferenceManager mgr;
    private String title = "";
    private final NodeContextTracker nodeContextTracker = new NodeContextTracker();

    public AbstractModel(Container root) {
        this(root, null);
    }

    public AbstractModel(Container root, ReferenceManager man) {
        this.root = root;
        if (man != null) {
            this.mgr = man;
        } else {
            if (root instanceof NamespaceProvider) {
                this.mgr = new HierarchicalUniqueNameReferenceManager();
            } else {
                this.mgr = new DefaultReferenceManager();
            }
        }
        this.nodeContextTracker.attach(root);
        this.mgr.attach(root);
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
        DisplayName name = this.getClass().getAnnotation(DisplayName.class);
        if (name == null) {
            return this.getClass().getSimpleName();
        } else {
            return name.value();
        }
    }

    @Override
    public String getShortName() {
        ShortName name = this.getClass().getAnnotation(ShortName.class);
        if (name != null) {
            return name.value();
        } else {
            String result = "";
            String s = getDisplayName();
            boolean b = true;
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (b && !Character.isSpaceChar(c) || Character.isUpperCase(c)) {
                    result += c;
                }
                b = Character.isSpaceChar(c);
            }
            return result;
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public final Container getRoot() {
        return root;
    }

    @Override
    public Set<Node> getPostset(Node component) {
        return nodeContextTracker.getPostset(component);
    }

    @Override
    public Set<Node> getPreset(Node component) {
        return nodeContextTracker.getPreset(component);
    }

    @Override
    public Set<Connection> getConnections(Node component) {
        return nodeContextTracker.getConnections(component);
    }

    @Override
    public boolean hasConnection(Node first, Node second) {
        return nodeContextTracker.hasConnection(first, second);
    }

    @Override
    public Connection getConnection(Node first, Node second) {
        return nodeContextTracker.getConnection(first, second);
    }

    @Override
    public <T> Set<T> getPreset(Node node, Class<T> type) {
        Set<T> result = new HashSet<>();
        for (Node pred: getPreset(node)) {
            try {
                result.add(type.cast(pred));
            } catch (ClassCastException e) {
            }
        }
        return result;
    }

    @Override
    public <T> Set<T> getPostset(Node node, Class<T> type) {
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
    public <T> Set<T> getPreset(Node node, Class<T> type, Func<Node, Boolean> through) {
        Set<T> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node cur = queue.remove();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (Node pred: getPreset(cur)) {
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
    public <T> Set<T> getPostset(Node node, Class<T> type, Func<Node, Boolean> through) {
        Set<T> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            Node cur = queue.remove();
            if (visited.contains(cur)) continue;
            visited.add(cur);
            for (Node succ: getPostset(cur)) {
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
    public Node getNodeByReference(String reference) {
        return getNodeByReference(null, reference);
    }

    @Override
    public String getNodeReference(Node node) {
        return getNodeReference(null, node);
    }

    @Override
    public Node getNodeByReference(NamespaceProvider provider, String reference) {
        return mgr.getNodeByReference(provider, reference);
    }

    @Override
    public String getNodeReference(NamespaceProvider provider, Node node) {
        return mgr.getNodeReference(provider, node);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = new ModelProperties();
        if ((node != null) && !(node instanceof Connection)) {
            properties.add(new NamePropertyDescriptor(this, node));
        }
        return properties;
    }

    @Override
    public ReferenceManager getReferenceManager() {
        return mgr;
    }

    @Override
    public String getName(Node node) {
        if (mgr instanceof HierarchicalUniqueNameReferenceManager) {
            return ((HierarchicalUniqueNameReferenceManager) mgr).getName(node);
        }
        return mgr.getNodeReference(null, node);
    }

    @Override
    public void setName(Node node, String name) {
        if (mgr instanceof HierarchicalUniqueNameReferenceManager) {
            ((HierarchicalUniqueNameReferenceManager) mgr).setName(node, name);
        }
    }

    public void reparent(Container targetContainer, Model sourceModel, Collection<Node> sourceNodes) {
        // reparent for the general model undefined?
    }

}
