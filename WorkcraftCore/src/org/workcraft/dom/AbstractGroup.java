package org.workcraft.dom;

import org.workcraft.observation.*;
import org.workcraft.util.Hierarchy;

import java.util.Collection;
import java.util.LinkedList;

public abstract class AbstractGroup implements Container, ObservableHierarchy {
    private Node parent = null;
    private final ObservableHierarchyImpl observableHierarchyImpl = new ObservableHierarchyImpl();
    private final Container groupRef;

    AbstractGroup(Container groupRef) {
        this.groupRef = groupRef;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        if (parent != this.parent) {
            if (parent != null && this.parent != null) {
                throw new RuntimeException("Cannot assign new parent to a node that already has a parent.");
            }
            this.parent = parent;
        }
    }

    protected void addInternal(Node node, boolean notify) {
        preAdd(node, notify);
        addInternal(node);
        postAdd(node, notify);
    }

    protected void postAdd(Node node, boolean notify) {
        node.setParent(groupRef);
        if (notify) {
            observableHierarchyImpl.sendNotification(new NodesAddedEvent(groupRef, node));
        }
    }

    protected void preAdd(Node node, boolean notify) {
        if (node.getParent() == this) {
            return;
        }
        if ((node.getParent() != null) && (node.getParent() != groupRef)) {
            throw new RuntimeException("Cannot attach someone else's node. Please detach from the old parent first.");
        }
        if (notify) {
            observableHierarchyImpl.sendNotification(new NodesAddingEvent(groupRef, node));
        }
    }

    protected void removeInternal(Node node, boolean notify) {
        Node parent = node.getParent();
        if (parent == groupRef) {
            if (notify) {
                observableHierarchyImpl.sendNotification(new NodesDeletingEvent(groupRef, node));
            }
            removeInternal(node);
            node.setParent(null);
            if (notify) {
                observableHierarchyImpl.sendNotification(new NodesDeletedEvent(groupRef, node));
            }

        } else if (parent != null) {
            throw new RuntimeException(
                    "Failed to remove a node frome a group because it is not a child of that group ("
                    + node + ", parent is " + parent + ", expected " + groupRef + ")");
        }
    }

    public void addObserver(HierarchyObserver obs) {
        observableHierarchyImpl.addObserver(obs);
    }

    public void removeObserver(HierarchyObserver obs) {
        observableHierarchyImpl.removeObserver(obs);
    }

    public void removeAllObservers() {
        observableHierarchyImpl.removeAllObservers();
    }

    @Override
    public void add(Node node) {
        addInternal(node, true);
    }

    @Override
    public void add(Collection<? extends Node> nodes) {
        observableHierarchyImpl.sendNotification(new NodesAddingEvent(groupRef, nodes));
        for (Node node : nodes) {
            addInternal(node, false);
        }
        observableHierarchyImpl.sendNotification(new NodesAddedEvent(groupRef, nodes));
    }

    @Override
    public void remove(Node node) {
        removeInternal(node, true);
    }

    public void removeWithoutNotify(Node node) {
        removeInternal(node, false);
    }

    @Override
    public void remove(Collection<? extends Node> nodes) {
        LinkedList<Node> nodesToRemove = new LinkedList<>(nodes);
        observableHierarchyImpl.sendNotification(new NodesDeletingEvent(groupRef, nodesToRemove));
        for (Node node : nodesToRemove) {
            removeInternal(node, false);
        }
        observableHierarchyImpl.sendNotification(new NodesDeletedEvent(groupRef, nodesToRemove));
    }

    @Override
    public void reparent(Collection<? extends Node> nodes, Container newParent) {
        observableHierarchyImpl.sendNotification(new NodesReparentingEvent(groupRef, newParent, nodes));
        for (Node node: nodes) {
            removeInternal(node, false);
        }
        if (!isSameModel(nodes, newParent)) {
            // If the model is not the same, then remove all node listeners
            for (Node node: nodes) {
                if (node instanceof ObservableHierarchy) {
                    ((ObservableHierarchy) node).removeAllObservers();
                }
            }
        }
        newParent.add(nodes);
        observableHierarchyImpl.sendNotification(new NodesReparentedEvent(groupRef, newParent, nodes));
    }

    private boolean isSameModel(Collection<? extends Node> nodes, Container newParent) {
        Node newRoot = Hierarchy.getTopParent(newParent);
        boolean sameModel = true;
        for (Node node : nodes) {
            if (newRoot != Hierarchy.getTopParent(node)) {
                sameModel = false;
                break;
            }
        }
        return sameModel;
    }

    @Override
    public void reparent(Collection<? extends Node> nodes) {
        for (Node node : nodes) {
            addInternal(node, false);
        }
    }

    @Override
    public abstract Collection<Node> getChildren();

    protected abstract void addInternal(Node node);

    protected abstract void removeInternal(Node node);
}
