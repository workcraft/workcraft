package org.workcraft.dom.visual;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;

import java.util.HashMap;
import java.util.LinkedList;

public class TransformEventPropagator extends HierarchySupervisor implements StateObserver, TransformDispatcher {

    private final HashMap<Node, LinkedList<TransformObserver>> nodeToObservers = new HashMap<>();
    private final HashMap<TransformObserver, LinkedList<Node>> observerToNodes = new HashMap<>();

    private void addObserver(Node node, TransformObserver to) {
        nodeToObservers.computeIfAbsent(node, k -> new LinkedList<>()).add(to);
    }

    private void removeObserver(Node node, TransformObserver to) {
        LinkedList<TransformObserver> list = nodeToObservers.get(node);
        list.remove(to);
        if (list.isEmpty()) {
            nodeToObservers.remove(node);
        }
    }

    private void addObservedNode(TransformObserver to, Node node) {
        observerToNodes.computeIfAbsent(to, k -> new LinkedList<>()).add(node);
    }

    private void removeObservedNode(TransformObserver to, Node node) {
        LinkedList<Node> list = observerToNodes.get(to);
        list.remove(node);
        if (list.isEmpty()) {
            observerToNodes.remove(to);
        }
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesAddedEvent || e instanceof NodesReparentedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesDeletedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeRemoved(n);
            }
        }
    }

    private void nodeRemoved(Node node) {
        if (node instanceof ObservableState) {
            ((ObservableState) node).removeObserver(this);

            // remove node from all observer lists
            LinkedList<TransformObserver> observers = nodeToObservers.get(node);
            if (observers != null) {
                for (TransformObserver to : observers) {
                    removeObservedNode(to, node);
                }
            }

            nodeToObservers.remove(node);
        }

        if (node instanceof TransformObserver to) {
            LinkedList<Node> nodes = observerToNodes.get(to);
            if (nodes != null) {
                for (Node n : nodes) {
                    removeObserver(n, to);
                }
            }
        }

        for (Node n : node.getChildren()) {
            nodeRemoved(n);
        }
    }

    private void nodeAdded(Node node) {
        if (node instanceof ObservableState) {
            ((ObservableState) node).addObserver(this);
        }

        if (node instanceof TransformObserver to) {
            to.subscribe(this);
        }

        for (Node n : node.getChildren()) {
            nodeAdded(n);
        }
    }

    private void propagate(Node node, TransformEvent e) {
        LinkedList<TransformObserver> list = nodeToObservers.get(node);
        if (list != null) {
            for (TransformObserver to : list) {
                to.notify(e);
            }
        }

        for (Node n : node.getChildren()) {
            propagate(n, e);
        }
    }

    @Override
    public void notify(StateEvent e) {
        if (e instanceof TransformEvent) {
            propagate(((TransformEvent) e).getSender(), (TransformEvent) e);
        }
    }

    @Override
    public void subscribe(TransformObserver observer, Node observed) {
        addObserver(observed, observer);
        addObservedNode(observer, observed);
    }

    @Override
    public void unsubscribe(TransformObserver observer, Node observed) {
        removeObserver(observed, observer);
        removeObservedNode(observer, observed);
    }

}
