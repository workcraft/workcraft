package org.workcraft.plugins.cpog;

import net.sf.jga.fn.UnaryFunctor;
import org.workcraft.dom.*;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.NodesDeletingEvent;

import java.util.HashSet;

public class CpogHangingConnectionRemover extends DefaultHangingConnectionRemover {

    public CpogHangingConnectionRemover(NodeContext nct) {
        super(nct);
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
                if (con.getParent() instanceof VisualPage) {
                    ((VisualPage) con.getParent()).removeWithoutNotify(con);
                } else if (con.getParent() instanceof VisualGroup) {
                    ((VisualGroup) con.getParent()).removeWithoutNotify(con);
                } else if (con.getParent() != null) {
                    throw new RuntimeException("Cannot remove a hanging connection because its parent is not a Container.");
                }
            }

        }
    }

}
