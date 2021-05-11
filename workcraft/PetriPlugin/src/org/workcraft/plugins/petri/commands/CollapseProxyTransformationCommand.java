package org.workcraft.plugins.petri.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.utils.ConnectionUtils;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class CollapseProxyTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Collapse proxy places (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Collapse proxy place";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualReplicaPlace;
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
        Collection<VisualNode> replicas = new HashSet<>();
        // Collect selected (or all) replicas
        replicas.addAll(ConnectionUtils.getVisualReplicaPlaces(model));
        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            replicas.retainAll(selection);
        }
        // Collect replicas on selected (or all) read-arcs
        HashSet<VisualReadArc> readArcs = ConnectionUtils.getVisualReadArcs(model);
        if (!selection.isEmpty()) {
            readArcs.retainAll(selection);
        }
        if (!readArcs.isEmpty()) {
            for (VisualReadArc readArc: readArcs) {
                if (readArc.getFirst() instanceof VisualReplicaPlace) {
                    VisualReplicaPlace replica = (VisualReplicaPlace) readArc.getFirst();
                    replicas.add(replica);
                }
            }
        }
        return replicas;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if (node instanceof VisualReplicaPlace) {
            VisualReplicaPlace replicaPlace = (VisualReplicaPlace) node;
            ConversionUtils.collapseReplicaPlace(model, replicaPlace);
        }
    }

}
