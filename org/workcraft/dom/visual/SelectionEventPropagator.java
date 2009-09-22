package org.workcraft.dom.visual;

import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.SelectionObserver;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;

public class SelectionEventPropagator extends HierarchySupervisor implements StateObserver {
	private LinkedList<SelectionObserver> selectionObservers = new LinkedList<SelectionObserver>();

	public SelectionEventPropagator (VisualModel model) {
		model.addObserver(this);
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent) {
			for (Node n:e.getAffectedNodes())
				nodeAdded(n);
		} else if (e instanceof NodesDeletedEvent) {
			for (Node n:e.getAffectedNodes())
				nodeRemoved(n);
		}
	}

	private void nodeRemoved(Node node) {
		if (node instanceof SelectionObserver) {
			//System.out.println ("Removing observer " + node);
			SelectionObserver so = (SelectionObserver)node;
			selectionObservers.remove(so);
		}

		for (Node n : node.getChildren())
			nodeRemoved(n);
	}

	private void nodeAdded(Node node) {
		if (node instanceof SelectionObserver) {
			//System.out.println ("Adding observer " + node);
			SelectionObserver so = (SelectionObserver) node;
			selectionObservers.add(so);
		}

		for (Node n : node.getChildren())
			nodeAdded(n);
	}

	@Override
	public void notify(StateEvent e) {
		if (e instanceof SelectionChangedEvent) {
			//System.out.println ("Propagating event");
			for (SelectionObserver so : selectionObservers)
				so.notify((SelectionChangedEvent)e);
		}
	}
}