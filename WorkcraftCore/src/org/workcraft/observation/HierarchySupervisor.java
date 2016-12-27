package org.workcraft.observation;

import org.workcraft.dom.Node;

public abstract class HierarchySupervisor implements HierarchyObserver {
    private Node root = null;

    protected Node getRoot() {
        return root;
    }

    private void attachInternal(Node root) {
        if (root instanceof ObservableHierarchy) {
            ((ObservableHierarchy) root).addObserver(this);
        }
        for (Node node : root.getChildren()) {
            attachInternal(node);
        }
    }

    public void attach(Node root, boolean sendRootAddedEvent) {
        this.root = root;
        if (sendRootAddedEvent) {
            handleEvent(new NodesAddedEvent(root.getParent(), root));
        }
        attachInternal(root);
    }

    public void attach(Node root) {
        attach(root, true);
    }

    private void detachInternal(Node root) {
        if (root instanceof ObservableHierarchy) {
            ((ObservableHierarchy) root).removeObserver(this);
        }
        for (Node node : root.getChildren()) {
            detachInternal(node);
        }
    }

    public void detach() {
        detachInternal(root);
        this.root = null;
    }

    @Override
    public void notify(HierarchyEvent e) {
        if (e instanceof NodesDeletedEvent) {
            for (Node n : e.getAffectedNodes()) {
                detachInternal(n);
            }
        } else if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                attachInternal(n);
            }
        }
        handleEvent(e);
    }

    public abstract void handleEvent(HierarchyEvent e);

}
