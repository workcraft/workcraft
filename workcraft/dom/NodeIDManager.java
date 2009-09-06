package org.workcraft.dom;

import java.util.HashMap;

import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

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
				int id = idGenerator.getNextID();

				nodes.put(id, n);
				nodeIdentifiers.put(n, id);
			}
		} else if (e instanceof NodesDeletedEvent) {
			for (Node n: e.getAffectedNodes()) {
				nodes.remove(nodeIdentifiers.get(n));
				nodeIdentifiers.remove(n);
			}
		}
	}
}
