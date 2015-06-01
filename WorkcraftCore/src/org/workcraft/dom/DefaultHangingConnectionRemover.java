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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import net.sf.jga.fn.UnaryFunctor;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.util.Hierarchy;


public class DefaultHangingConnectionRemover extends HierarchySupervisor {
	private NodeContext nct;
	private String id;

	public DefaultHangingConnectionRemover (NodeContext nct, String id) {
		this.nct = nct;
	}

	@SuppressWarnings("serial")
	@Override
	public void handleEvent(final HierarchyEvent e) {
		if (e instanceof NodesDeletingEvent) {
			HashSet<Connection> hangingConnections = new HashSet<>();
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
					((Container) con.getParent()).remove(con);
				else
					throw new RuntimeException("Cannot remove a hanging connection because its parent is not a Container.");

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