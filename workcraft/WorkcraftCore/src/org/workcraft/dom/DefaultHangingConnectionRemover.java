package org.workcraft.dom;

import net.sf.jga.fn.UnaryFunctor;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.utils.Hierarchy;

import java.util.Collection;
import java.util.HashSet;

public class DefaultHangingConnectionRemover extends HierarchySupervisor {
    private final NodeContext nct;

    public DefaultHangingConnectionRemover(NodeContext nct) {
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
                    return !isConnectionInside(e.getAffectedNodes(), arg0);
                }
            };

            for (Node node : e.getAffectedNodes()) {
                findHangingConnections(node, hangingConnections, hanging);
            }

            for (Connection con : hangingConnections) {
                Node parent = con.getParent();
                if (parent instanceof Container) {
                    ((Container) parent).remove(con);
                } else if (parent != null) {
                    throw new RuntimeException("Cannot remove a hanging connection because its parent is not a Container.");
                }
            }
        }
    }

    public static boolean isConnectionInside(Collection<Node> nodes, Connection con) {
        for (Node node : nodes) {
            if (node == con || Hierarchy.isDescendant(con, node)) {
                return true;
            }
        }
        return false;
    }

    public void findHangingConnections(Node node, HashSet<Connection> hangingConnections, UnaryFunctor<Connection, Boolean> hanging) {
        // need only to remove those connections that are not already being deleted
        for (Object o : nct.getConnections(node)) {
            if (o instanceof Connection) {
                Connection connection = (Connection) o;
                if (hanging.fn(connection)) {
                    hangingConnections.add(connection);
                }
            }
        }
        for (Node nn : node.getChildren()) {
            findHangingConnections(nn, hangingConnections, hanging);
        }
    }

}
