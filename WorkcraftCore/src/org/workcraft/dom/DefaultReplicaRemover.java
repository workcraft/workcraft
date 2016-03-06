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

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.Replicable;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.util.Hierarchy;

public class DefaultReplicaRemover extends HierarchySupervisor {
    private NodeContext nct;

    public DefaultReplicaRemover(NodeContext nct) {
        this.nct = nct;
    }

    @SuppressWarnings("serial")
    @Override
    public void handleEvent(final HierarchyEvent e) {
        if (e instanceof NodesDeletedEvent) {
            Collection<Node> affectedNodes = e.getAffectedNodes();
            for (Node node : affectedNodes) {
                // Update a master node if its replicas are being deleted (even deeper in hierarchy)
                for (Replica replica: Hierarchy.getDescendantsOfType(node, Replica.class)) {
                    updateReplicable(replica);
                }
                if (node instanceof Replica) {
                    updateReplicable((Replica) node);
                }
                // Remove replicas if the master node is being deleted (even deeper in hierarchy)
                for (Replicable replicable: Hierarchy.getDescendantsOfType(node, Replicable.class)) {
                    removeReplicas(replicable);
                }
                if (node instanceof Replicable) {
                    removeReplicas((Replicable) node);
                }
            }
            for (Node node : affectedNodes) {
                // Remove a replica if all its connections have been removed
                if (node instanceof Connection) {
                    Connection connection = (Connection) node;
                    Node replica = null;
                    if (connection.getFirst() instanceof Replica) {
                        replica = connection.getFirst();
                    }
                    if (connection.getSecond() instanceof Replica) {
                        replica = connection.getSecond();
                    }
                    if (replica instanceof Replica) {
                        HashSet<Connection> connections = new HashSet<>(nct.getConnections(replica));
                        connections.removeAll(affectedNodes);
                        if (connections.isEmpty()) {
                            removeReplica((Replica) replica);
                        }
                    }
                }
                if (node instanceof Replicable) {
                    removeReplicas((Replicable) node);
                }
            }
        }
    }

    private void removeReplicas(Replicable replicable) {
        for (Replica replica: new HashSet<>(replicable.getReplicas())) {
            removeReplica(replica);
        }
    }

    private void removeReplica(Replica replica) {
        if (replica instanceof Node) {
            Node replicaNode = (Node) replica;
            Node parent = replicaNode.getParent();
            if (parent instanceof Container) {
                ((Container) parent).remove(replicaNode);
            }
        }
    }

    private void updateReplicable(Replica replica) {
        Replicable replicable = replica.getMaster();
        if (replicable != null) {
            replicable.removeReplica(replica);
            replica.setMaster(null);
        }
    }

}
