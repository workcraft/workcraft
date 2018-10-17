package org.workcraft.plugins.petri.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
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
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> replicas = new HashSet<>();
        // Collect selected (or all) replicas
        replicas.addAll(PetriNetUtils.getVisualReplicaPlaces(model));
        Collection<VisualNode> selection = model.getSelection();
        if (!selection.isEmpty()) {
            replicas.retainAll(selection);
        }
        // Collect replicas on selected (or all) read-arcs
        HashSet<VisualReadArc> readArcs = PetriNetUtils.getVisualReadArcs(model);
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
    public void transform(VisualModel model, VisualNode node) {
        if (node instanceof VisualReplicaPlace) {
            VisualReplicaPlace replicaPlace = (VisualReplicaPlace) node;
            PetriNetUtils.collapseReplicaPlace(model, replicaPlace);
        }
    }

}
