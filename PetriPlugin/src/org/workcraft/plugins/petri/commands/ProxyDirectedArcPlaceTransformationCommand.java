package org.workcraft.plugins.petri.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        boolean result = false;
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) node;
            Node place = null;
            if (PetriNetUtils.isVisualConsumingArc(connection)) {
                place = connection.getFirst();
            } else if (PetriNetUtils.isVisualProducingArc(connection)) {
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
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> connections = new HashSet<>();
        connections.addAll(PetriNetUtils.getVisualProducingArcs(model));
        connections.addAll(PetriNetUtils.getVisualConsumingArcs(model));
        connections.retainAll(model.getSelection());
        return connections;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = PetriNetUtils.replicateConnectedPlace(model, (VisualConnection) node);
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
