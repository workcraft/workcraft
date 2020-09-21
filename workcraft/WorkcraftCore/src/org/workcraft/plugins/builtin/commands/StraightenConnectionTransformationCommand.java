package org.workcraft.plugins.builtin.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class StraightenConnectionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Straighten connections (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Straighten connection";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualConnection;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        boolean result = false;
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            ConnectionGraphic graphic = connection.getGraphic();
            if (graphic instanceof Bezier) {
                result = true;
            } else if (graphic instanceof Polyline) {
                Polyline polyline = (Polyline) graphic;
                result = polyline.getControlPointCount() > 0;
            }
        }
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> connections = new HashSet<>();
        connections.addAll(Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class));
        Collection<? extends VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            HashSet<Node> selectedConnections = new HashSet<>(selection);
            selectedConnections.retainAll(connections);
            if (!selectedConnections.isEmpty()) {
                connections.retainAll(selection);
            }
        }
        return connections;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            connection.setConnectionType(ConnectionType.BEZIER);
            connection.setConnectionType(ConnectionType.POLYLINE);
        }
    }

}
