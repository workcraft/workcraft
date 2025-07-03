package org.workcraft.dom;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class DefaultHangingConnectionRemover extends HierarchySupervisor {

    private final NodeContext<? extends Node, ? extends Connection> nct;

    public DefaultHangingConnectionRemover(NodeContext<? extends Node, ? extends Connection> nct) {
        this.nct = nct;
    }

    @Override
    public void handleEvent(final HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            HashSet<Connection> hangingConnections = new HashSet<>();

            for (Node node : e.getAffectedNodes()) {
                findHangingConnections(node, hangingConnections, c -> isConnectionOutside(e.getAffectedNodes(), c));
            }

            for (Connection connection : hangingConnections) {
                Node parent = connection.getParent();
                if (parent instanceof Container) {
                    ((Container) parent).remove(connection);
                } else if (parent != null) {
                    throw new RuntimeException("Cannot remove a hanging connection because its parent is not a Container.");
                }
            }
        }
    }

    public static boolean isConnectionOutside(Collection<Node> nodes, Connection connection) {
        for (Node node : nodes) {
            if ((node == connection) || Hierarchy.isDescendant(connection, node)) {
                return false;
            }
        }
        return true;
    }

    public void findHangingConnections(Node node, Set<Connection> hangingConnections,
            Function<Connection, Boolean> hanging) {

        // need only to remove those connections that are not already being deleted
        for (Connection connection : nct.getConnections(node)) {
            if (hanging.apply(connection)) {
                hangingConnections.add(connection);
            }
        }
        for (Node childNode : node.getChildren()) {
            findHangingConnections(childNode, hangingConnections, hanging);
        }
    }

}
