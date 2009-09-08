package org.workcraft.framework.observation;

import org.workcraft.dom.Node;


public abstract class StateSupervisor extends HierarchySupervisor implements StateObserver {

	private void nodeAdded (Node node) {
		if (node instanceof ObservableState)
			((ObservableState)node).addObserver(this);

		for (Node n : node.getChildren())
			nodeAdded(n);
	}

	private void nodeRemoved (Node node) {
		if (node instanceof ObservableState)
			((ObservableState)node).removeObserver(this);

		for (Node n : node.getChildren())
			nodeRemoved(n);
	}

	final public void handleEvent (HierarchyEvent e)	{
		if (e instanceof NodesAddedEvent) {
			for (Node n : e.getAffectedNodes())
				nodeAdded(n);

		} else if (e instanceof NodesDeletedEvent) {
			for (Node n : e.getAffectedNodes())
				nodeRemoved(n);
		}

		handleHierarchyEvent (e);
	}

	@Override
	final public void notify(StateEvent e) {
		handleEvent(e);
	}

	public void handleHierarchyEvent (HierarchyEvent e) {

	}

	public abstract void handleEvent (StateEvent e);
}