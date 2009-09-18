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
