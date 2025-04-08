package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class CollapseProxyTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Collapse proxy places (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return (node instanceof Replica) ? "Collapse proxy place" : "Collapse all proxies of place";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualReplicaPlace) || (node instanceof VisualPlace);
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return (node instanceof VisualReplicaPlace)
                || ((node instanceof VisualPlace place) && !place.getReplicas().isEmpty());
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        // Collect selected (or all) replicas
        Collection<VisualNode> result = new HashSet<>(ConnectionUtils.getVisualReplicaPlaces(model));
        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            result.retainAll(selection);
            // Collect replicas of selected places
            for (VisualNode node : selection) {
                if (node instanceof VisualPlace place) {
                    for (Replica replica : place.getReplicas()) {
                        if (replica instanceof VisualReplicaPlace replicaPlace) {
                            result.add(replicaPlace);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualReplicaPlace replicaPlace) {
            ConversionUtils.collapseReplicaPlace(model, replicaPlace);
        }
    }

}
