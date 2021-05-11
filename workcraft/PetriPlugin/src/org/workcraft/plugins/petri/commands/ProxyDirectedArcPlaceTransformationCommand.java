package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class ProxyDirectedArcPlaceTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Create proxies for selected producing/consuming arc places";
    }

    @Override
    public String getPopupName() {
        return "Create proxy place";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        boolean result = false;
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            Node place = null;
            if (ConnectionUtils.isVisualConsumingArc(connection)) {
                place = connection.getFirst();
            } else if (ConnectionUtils.isVisualProducingArc(connection)) {
                place = connection.getSecond();
            }
            result = place instanceof VisualPlace;
        }
        return result;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> connections = new HashSet<>();
        connections.addAll(ConnectionUtils.getVisualProducingArcs(model));
        connections.addAll(ConnectionUtils.getVisualConsumingArcs(model));
        connections.retainAll(model.getSelection());
        return connections;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = ConversionUtils.replicateConnectedPlace(model, (VisualConnection) node);
            if (connection != null) {
                if (connection.getFirst() instanceof VisualReplicaPlace) {
                    model.addToSelection(connection.getFirst());
                }
                if (connection.getSecond() instanceof VisualReplicaPlace) {
                    model.addToSelection(connection.getSecond());
                }
            }
        }
    }

}
