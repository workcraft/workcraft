package org.workcraft.plugins.petri.tools;

import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.workspace.WorkspaceEntry;

public class ProxyDirectedArcPlaceTool extends TransformationTool implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Create proxies for selected producing/consuming arc places";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof PetriNetModel;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        if (node instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection)node;
            Node place = null;
            if (PetriNetUtils.isVisualConsumingArc(connection)) {
                place = connection.getFirst();
            } else if (PetriNetUtils.isVisualProducingArc(connection)) {
                place = connection.getSecond();
            }
            return place instanceof VisualPlace;
        }
        return false;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualModel model = we.getModelEntry().getVisualModel();
        HashSet<VisualConnection> connections = new HashSet<>();
        connections.addAll(PetriNetUtils.getVisualProducingArcs(model));
        connections.addAll(PetriNetUtils.getVisualConsumingArcs(model));
        connections.retainAll(model.getSelection());
        if ( !connections.isEmpty() ) {
            we.saveMemento();
            for (VisualConnection connection: connections) {
                transform(model, connection);
            }
            model.selectNone();
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualConnection)) {
            VisualModel visualModel = (VisualModel)model;
            VisualConnection connection = (VisualConnection)node;
            PetriNetUtils.replicateConnectedPlace(visualModel, connection);
        }
    }

}
