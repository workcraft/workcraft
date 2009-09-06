package org.workcraft.dom;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.framework.observation.HierarchyEvent;
import org.workcraft.framework.observation.HierarchySupervisor;
import org.workcraft.framework.observation.NodesDeletingEvent;

public class DefaultHangingConnectionRemover extends HierarchySupervisor {
	private NodeContext nct;

	public DefaultHangingConnectionRemover (NodeContext nct) {
		this.nct = nct;
	}

	@Override
	public void handleEvent(HierarchyEvent e) {
		if (e instanceof NodesDeletingEvent) {
			for (Node n: e.getAffectedNodes()) {
				Set<Connection> connectionsToDelete = new HashSet<Connection>(nct.getConnections((Node)n));
				for (Connection con : connectionsToDelete)
					if (con.getParent() instanceof Container)
						((Container)con.getParent()).remove(con);
					else
						throw new RuntimeException ("Hanging connection cannot be removed because its parent is not a Container.");
			}
		}
	}

}