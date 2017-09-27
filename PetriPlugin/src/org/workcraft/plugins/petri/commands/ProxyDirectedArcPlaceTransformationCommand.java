package org.workcraft.plugins.petri.commands;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public boolean isApplicableTo(Node node) {
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
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> connections = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            connections.addAll(PetriNetUtils.getVisualProducingArcs(visualModel));
            connections.addAll(PetriNetUtils.getVisualConsumingArcs(visualModel));
            connections.retainAll(visualModel.getSelection());
        }
        return connections;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualConnection)) {
            VisualModel visualModel = (VisualModel) model;
            VisualConnection connection = (VisualConnection) node;
            PetriNetUtils.replicateConnectedPlace(visualModel, connection);
        }
    }

}
