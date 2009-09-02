package org.workcraft.dom;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchyObserver;

public class NodeContextTracker <CompT extends Component,ConT extends Connection<CompT>> implements HierarchyObserver {
	HashMap<CompT, LinkedHashSet<CompT>> presets = new HashMap<CompT, LinkedHashSet<CompT>>();
	HashMap<CompT, LinkedHashSet<CompT>> postsets = new HashMap<CompT, LinkedHashSet<CompT>>();
	HashMap<CompT, LinkedHashSet<ConT>> connections = new HashMap<CompT, LinkedHashSet<ConT>>();

	@SuppressWarnings("unchecked")
	public void notify(HierarchyEvent e) {
		switch (e.getEventType()) {
		case NODES_ADDED:
			for (HierarchyNode n : e.getAffectedNodes()) {
				if (n instanceof Component)
					presets.put((CompT)n, new LinkedHashSet<CompT>());

				if (n instanceof Connection) {
					ConT con = (ConT)n;
					CompT c1 = con.getFirst();
					CompT c2 = con.getSecond();

					postsets.get(c1).add(c2);
					presets.get(c2).add(c1);
					connections.get(c1).add(con);
					connections.get(c2).add(con);
				}
			}
			break;
		case NODES_REMOVED:
			for (HierarchyNode n : e.getAffectedNodes()) {
				if (n instanceof Component)
					presets.remove((Component)n);
				if (n instanceof Connection) {
					ConT con = (ConT)n;
					CompT c1 = con.getFirst();
					CompT c2 = con.getSecond();

					postsets.get(c1).remove(c2);
					presets.get(c2).remove(c1);
					connections.get(c1).remove(con);
					connections.get(c2).remove(con);
				}
			}
			break;

		}
	}

	public Set<CompT> getPreset(CompT component) {
		return Collections.unmodifiableSet(presets.get(component));
	}

	public Set<CompT> getPostset(CompT component) {
		return Collections.unmodifiableSet(presets.get(component));
	}

	public Set<ConT> getConnections (CompT component) {
		return Collections.unmodifiableSet(connections.get(component));
	}
}