package org.workcraft.dom;

import org.workcraft.annotations.Annotations;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.*;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.util.Func;

import java.util.*;

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
                this.mgr = new UniqueReferenceManager();
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
            String name = getName(node);
            if ((name != null) && !Identifier.isInternal(name)) {
                properties.add(new NamePropertyDescriptor(this, node));
            }
        }
        return properties;
    }

    @Override
    public ReferenceManager getReferenceManager() {
        return mgr;
    }

    @Override
    public String getName(Node node) {
        if (mgr instanceof UniqueReferenceManager) {
            return ((UniqueReferenceManager) mgr).getName(node);
        }
        return mgr.getNodeReference(null, node);
    }

    @Override
    public void setName(Node node, String name) {
        if (mgr instanceof UniqueReferenceManager) {
            ((UniqueReferenceManager) mgr).setName(node, name);
        }
    }

    @Override
    public String getDerivedName(Node node, Container container, String candidate) {
        String result = candidate;
        if ((mgr instanceof UniqueReferenceManager) && (container instanceof NamespaceProvider)) {
            UniqueReferenceManager manager = (UniqueReferenceManager) mgr;
            NameManager nameManager = manager.getNameManager((NamespaceProvider) container);
            result = nameManager.getDerivedName(null, candidate);
        }
        return result;
    }

}
