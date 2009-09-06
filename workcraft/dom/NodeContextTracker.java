package org.workcraft.dom;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesAddedEvent;
import org.workcraft.framework.observation.NodesDeletedEvent;

public class NodeContextTracker extends HierarchySupervisor implements NodeContext {
	HashMap<Node, LinkedHashSet<Node>> presets = new HashMap<Node, LinkedHashSet<Node>>();
	HashMap<Node, LinkedHashSet<Node>> postsets = new HashMap<Node, LinkedHashSet<Node>>();
	HashMap<Node, LinkedHashSet<Connection>> connections = new HashMap<Node, LinkedHashSet<Connection>>();

	private void initHashes (Node n) {
		LinkedHashSet<Node> set = presets.get(n);
		if (set == null)
			presets.put(n, new LinkedHashSet<Node>());

		set = postsets.get(n);
		if (set == null)
			postsets.put(n, new LinkedHashSet<Node>());

		LinkedHashSet<Connection> conSet = connections.get(n);
		if (conSet == null)
			connections.put(n, new LinkedHashSet<Connection>());
	}

	private void removeHashes (Node n) {
		presets.remove(n);
		postsets.remove(n);
		connections.remove(n);
	}

	private void nodeAdded (Node n) {
		//System.out.println ("(NCT) node added " + n);
		initHashes(n);

		if (n instanceof Connection) {
			Connection con = (Connection)n;
			Node c1 = con.getFirst();
			Node c2 = con.getSecond();

			initHashes(c1);
			initHashes(c2);

			postsets.get(c1).add(c2);
			presets.get(c2).add(c1);
			connections.get(c1).add(con);
			connections.get(c2).add(con);
		}
	}

	private void nodeRemoved(Node n) {
		//System.out.println ("(NCT) node removed " + n);

		for (Node postsetNodes: postsets.get(n))
			presets.get(postsetNodes).remove(n);

		for (Node presetNodes: presets.get(n))
			postsets.get(presetNodes).remove(n);

		removeHashes(n);

		if (n instanceof Connection) {
			Connection con = (Connection)n;
			Node c1 = con.getFirst();
			Node c2 = con.getSecond();

			LinkedHashSet<Node> set = postsets.get(c1);
			if (set != null)
				postsets.get(c1).remove(c2);

			set = presets.get(c2);
			if (set != null)
				presets.get(c2).remove(c1);

			LinkedHashSet<Connection> conSet = connections.get(c1);
			if (conSet != null)
				connections.get(c1).remove(con);
			conSet = connections.get(c2);
			if (conSet != null)
				connections.get(c2).remove(con);
		}
	}

	public Set<Node> getPreset(Node node) {
		return Collections.unmodifiableSet(presets.get(node));
	}

	public Set<Node> getPostset(Node node) {
		return Collections.unmodifiableSet(postsets.get(node));
	}

	public Set<Connection> getConnections (Node node) {
		return Collections.unmodifiableSet(connections.get(node));
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesAddedEvent) {
			for (Node n : e.getAffectedNodes())
				nodeAdded(n);
		} else if (e instanceof NodesDeletedEvent) {
			for (Node n : e.getAffectedNodes())
				nodeRemoved(n);
		}
	}
}