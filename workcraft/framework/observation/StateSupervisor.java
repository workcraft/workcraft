package org.workcraft.framework.observation;

import org.workcraft.dom.Node;


public abstract class StateSupervisor extends HierarchySupervisor implements StateObserver {

	final public void handleEvent (HierarchyEvent e)	{
		if (e instanceof NodesAddedEvent) {
			for (Node n : e.getAffectedNodes())
				if (n instanceof ObservableState)
					((ObservableState)n).addObserver(this);

		} else if (e instanceof NodesDeletedEvent) {
			for (Node n : e.getAffectedNodes())
				if (n instanceof ObservableState)
					((ObservableState)n).removeObserver(this);
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