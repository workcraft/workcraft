package org.workcraft.dom.visual;

import java.util.HashMap;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;
import org.workcraft.framework.observation.ObservableState;
import org.workcraft.framework.observation.StateEvent;
import org.workcraft.framework.observation.StateObserver;
import org.workcraft.framework.observation.TransformChangedEvent;
import org.workcraft.framework.observation.TransformObserver;

public class TransformEventPropagator extends HierarchySupervisor implements StateObserver {
	HashMap <Node, LinkedList<TransformObserver>> transformObservers
	= new HashMap<Node, LinkedList<TransformObserver>>();

	private void putObserver (Node node, TransformObserver to) {
		LinkedList<TransformObserver> list = transformObservers.get(node);
		if ( list == null ) {
			list = new LinkedList<TransformObserver>();
			transformObservers.put(node, list);
		}
		list.add(to);
	}

	private void removeObserver (Node node, TransformObserver to) {
		LinkedList<TransformObserver> list = transformObservers.get(node);
		list.remove(to);
		if (list.isEmpty())
			transformObservers.remove(node);
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent) {
			for (Node n:e.getAffectedNodes())
				initialiseNode(n);
		} else if (e instanceof NodesDeletedEvent) {
			for (Node n:e.getAffectedNodes())
				removeNode(n);
		}
	}

	private void removeNode(Node node) {
		if (node instanceof ObservableState)
			((ObservableState)node).removeObserver(this);

		if (node instanceof TransformObserver) {
			for ( Node n : ((TransformObserver)node).getObservedNodes() ) {
				removeObserver (n, (TransformObserver)node);
			}
		}
	}

	private void initialiseNode(Node node) {
		if (node instanceof ObservableState)
			((ObservableState)node).addObserver(this);
		if (node instanceof TransformObserver) {
			TransformObserver to = (TransformObserver) node;
			for (Node n : to.getObservedNodes())
				putObserver(n, to);
		}
	}

	private void propagate(Node node, TransformChangedEvent e) {
		LinkedList<TransformObserver> list = transformObservers.get(node);
		if (list != null)
			for (TransformObserver to : list)
				to.notify(e);

		for (Node n : node.getChildren())
			propagate(n,e);
	}

	@Override
	public void notify(StateEvent e) {
		if (e instanceof TransformChangedEvent)
			propagate(((TransformChangedEvent)e).getSender(), ((TransformChangedEvent)e));
	}
}