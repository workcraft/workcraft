package org.workcraft.plugins.transform;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualConnection;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
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
    public Collection<Node> collect(Model model) {
        Collection<Node> connections = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            connections.addAll(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class));
            Collection<Node> selection = visualModel.getSelection();
            if (!selection.isEmpty()) {
                HashSet<Node> selectedConnections = new HashSet<>(selection);
                selectedConnections.retainAll(connections);
                if (!selectedConnections.isEmpty()) {
                    connections.retainAll(selection);
                }
            }
        }
        return connections;
    }

    @Override
    public void transform(Model model, Node node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            connection.setConnectionType(ConnectionType.BEZIER);
            connection.setConnectionType(ConnectionType.POLYLINE);
        }
    }

}
