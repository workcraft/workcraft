package org.workcraft.dom;

import java.util.Collection;
import java.util.HashSet;

import net.sf.jga.fn.UnaryFunctor;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.util.Hierarchy;

public class DefaultHangingConnectionRemover extends HierarchySupervisor {
	private NodeContext nct;

	public DefaultHangingConnectionRemover (NodeContext nct, String id) {
		this.nct = nct;
	}

	@SuppressWarnings("serial")
	@Override
	public void handleEvent(final HierarchyEvent e) {
		if (e instanceof NodesDeletingEvent) {
			HashSet<Connection> hangingConnections = new HashSet<Connection>();

			UnaryFunctor<Connection, Boolean> hanging = new UnaryFunctor<Connection, Boolean>() {
				@Override
				public Boolean fn(Connection arg0) {
					return !isConnectionInside (e.getAffectedNodes(), arg0);
				}
			};

			for (Node node : e.getAffectedNodes())
				findHangingConnections(node, hangingConnections, hanging);

			for (Connection con : hangingConnections)
				if (con.getParent() instanceof Container)
					((Container)con.getParent()).remove(con);
				else
					throw new RuntimeException ("Cannot remove a hanging connection because its parent is not a Container.");

		}
	}

	private static boolean isConnectionInside (Collection<Node> nodes, Connection con) {
		for (Node node : nodes)
			if (node == con || Hierarchy.isDescendant(con, node))
				return true;
		return false;
	}

	private void findHangingConnections(Node node, HashSet<Connection> hangingConnections, UnaryFunctor<Connection, Boolean> hanging) {
		// need only to remove those connections that are not already being deleted

		for (Connection con : nct.getConnections(node))
			if (hanging.fn(con))
				hangingConnections.add(con);
		for (Node nn : node.getChildren())
			findHangingConnections (nn, hangingConnections, hanging);
	}

}