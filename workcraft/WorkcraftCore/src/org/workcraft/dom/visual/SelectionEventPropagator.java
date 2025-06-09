package org.workcraft.dom.visual;

import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.SelectionObserver;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesReparentedEvent;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;

public class SelectionEventPropagator extends HierarchySupervisor implements StateObserver {
    private final LinkedList<SelectionObserver> selectionObservers = new LinkedList<>();

    public SelectionEventPropagator(VisualModel model) {
        model.addObserver(this);
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
        if (node instanceof SelectionObserver so) {
            //System.out.println("Removing observer " + node);
            selectionObservers.remove(so);
        }

        for (Node n : node.getChildren()) {
            nodeRemoved(n);
        }
    }

    private void nodeAdded(Node node) {
        if (node instanceof SelectionObserver so) {
            //System.out.println("Adding observer " + node);
            selectionObservers.add(so);
        }

        for (Node n : node.getChildren()) {
            nodeAdded(n);
        }
    }

    @Override
    public void notify(StateEvent e) {
        if (e instanceof SelectionChangedEvent) {
            //System.out.println("Propagating event");
            for (SelectionObserver so : selectionObservers) {
                so.notify((SelectionChangedEvent) e);
            }
        }
    }
}
