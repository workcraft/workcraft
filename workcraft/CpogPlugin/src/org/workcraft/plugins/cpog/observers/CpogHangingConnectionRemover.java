package org.workcraft.plugins.cpog.observers;

import org.workcraft.dom.Connection;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.NodeContext;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.NodesDeletingEvent;

import java.util.HashSet;

public class CpogHangingConnectionRemover extends DefaultHangingConnectionRemover {

    public CpogHangingConnectionRemover(NodeContext<?, ?> nct) {
        super(nct);
    }

    @Override
    public void handleEvent(final HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            HashSet<Connection> hangingConnections = new HashSet<>();
            for (Node node : e.getAffectedNodes()) {
                findHangingConnections(node, hangingConnections, c -> isConnectionOutside(e.getAffectedNodes(), c));
            }

            for (Connection connection : hangingConnections) {
                if (connection.getParent() instanceof VisualPage) {
                    ((VisualPage) connection.getParent()).removeWithoutNotify(connection);
                } else if (connection.getParent() instanceof VisualGroup) {
                    ((VisualGroup) connection.getParent()).removeWithoutNotify(connection);
                } else if (connection.getParent() != null) {
                    throw new RuntimeException("Cannot remove a hanging connection because its parent is not a Container.");
                }
            }
        }
    }

}
