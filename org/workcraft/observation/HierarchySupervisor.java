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

package org.workcraft.observation;

import org.workcraft.dom.Node;

public abstract class HierarchySupervisor implements HierarchyObserver {
	private Node root = null;

	private void attachInternal (Node root) {
		if (root instanceof ObservableHierarchy)
			((ObservableHierarchy)root).addObserver(this);

		for (Node node : root.getChildren())
			attachInternal (node);
	}

	public void attach (Node root) {
		this.root = root;

		handleEvent (new NodesAddedEvent(root.getParent(), root));

		attachInternal (root);
	}

	private void detachInternal (Node root) {
		if (root instanceof ObservableHierarchy)
			((ObservableHierarchy)root).removeObserver(this);

		for (Node node : root.getChildren())
			detachInternal (node);
	}

	public void detach () {
		detachInternal (root);
		this.root = null;
	}

	@Override
	public void notify(HierarchyEvent e) {
		if (e instanceof NodesDeletedEvent) {
			for (Node n : e.getAffectedNodes())
				detachInternal(n);
		} else if (e instanceof NodesAddedEvent) {
			for (Node n : e.getAffectedNodes())
				attachInternal(n);
		}

		handleEvent (e);
	}

	public abstract void handleEvent (HierarchyEvent e);
}