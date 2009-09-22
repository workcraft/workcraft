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

package org.workcraft.dom;

import java.util.HashMap;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;

public class NodeIDManager extends HierarchySupervisor {
	private IDGenerator idGenerator = new IDGenerator();

	private HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
	private HashMap<Node, Integer> nodeIdentifiers = new HashMap<Node, Integer>();

	public Node getNodeByID(int ID) {
		return nodes.get(ID);
	}

	public int getNodeID(Node node) {
		Integer ID = nodeIdentifiers.get(node);
		if (ID == null)
			return -1;
		else
			return ID;
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent)
		{
			for (Node n : e.getAffectedNodes()) {
				nodeAdded(n);
			}
		} else if (e instanceof NodesDeletedEvent) {
			for (Node n: e.getAffectedNodes()) {
				nodeRemoved(n);
			}
		}
	}

	private void nodeRemoved(Node n) {
		nodes.remove(nodeIdentifiers.get(n));
		nodeIdentifiers.remove(n);

		for (Node nn: n.getChildren())
			nodeRemoved(nn);
	}

	private void nodeAdded(Node n) {
		int id = idGenerator.getNextID();
		nodes.put(id, n);
		nodeIdentifiers.put(n, id);

		for (Node nn : n.getChildren())
			nodeAdded(nn);
	}
}
