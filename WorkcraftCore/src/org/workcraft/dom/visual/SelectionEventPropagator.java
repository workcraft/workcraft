/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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
	private LinkedList<SelectionObserver> selectionObservers = new LinkedList<SelectionObserver>();

	public SelectionEventPropagator (VisualModel model) {
		model.addObserver(this);
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent || e instanceof NodesReparentedEvent) {
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