package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class ProxyReadArcPlaceTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Create proxies for read-arc places (selected or all)";
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
        if (node instanceof VisualReadArc) {
            VisualReadArc readArc = (VisualReadArc) node;
            return readArc.getFirst() instanceof VisualPlace;
        }
        return false;
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
        Collection<VisualNode> readArcs = new HashSet<>();
        readArcs.addAll(PetriNetUtils.getVisualReadArcs(model));
        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            readArcs.retainAll(selection);
        }
        HashSet<VisualPlace> places = PetriNetUtils.getVisualPlaces(model);
        if (!selection.isEmpty()) {
            places.retainAll(selection);
        }
        for (VisualPlace place: places) {
            for (VisualConnection connection: model.getConnections(place)) {
                if (connection instanceof VisualReadArc) {
                    readArcs.add(connection);
                }
            }
        }
        return readArcs;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if (node instanceof VisualReadArc) {
            VisualConnection connection = PetriNetUtils.replicateConnectedPlace(model, (VisualReadArc) node);
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
