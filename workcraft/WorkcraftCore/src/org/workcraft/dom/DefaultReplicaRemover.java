package org.workcraft.dom;

import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.Replicable;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultReplicaRemover extends HierarchySupervisor {
    private final NodeContext nct;

    public DefaultReplicaRemover(NodeContext nct) {
        this.nct = nct;
    }

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
                if (node instanceof Connection connection) {
                    Node replica = null;
                    if (connection.getFirst() instanceof Replica) {
                        replica = connection.getFirst();
                    }
                    if (connection.getSecond() instanceof Replica) {
                        replica = connection.getSecond();
                    }
                    if (replica instanceof Replica) {
                        Set<Connection> connections = new HashSet<>(nct.getConnections(replica));
                        connections.removeAll(affectedNodes);
                        if (connections.isEmpty()) {
                            removeReplica((Replica) replica);
                        }
                    }
                }
                if (node instanceof Replicable replicable) {
                    removeReplicas(replicable);
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
        if (replica instanceof Node replicaNode) {
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
