package org.workcraft.plugins.stg.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.petri.utils.PetriNetUtils;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class ImplicitPlaceTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Make places implicit (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Make place implicit";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualStg.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualStgPlace;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        if (node instanceof VisualStgPlace) {
            VisualModel model = me.getVisualModel();
            VisualStgPlace place = (VisualStgPlace) node;
            Collection<VisualNode> preset = model.getPreset(place);
            Collection<VisualNode> postset = model.getPostset(place);
            Collection<Replica> replicas = place.getReplicas();
            if ((preset.size() == 1) && (postset.size() == 1) && replicas.isEmpty()) {
                VisualNode first = preset.iterator().next();
                VisualNode second = postset.iterator().next();
                if (!PetriNetUtils.hasImplicitPlaceArcConnection(model, first, second)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> places = new HashSet<>();
        if (model instanceof VisualStg) {
            VisualStg stg = (VisualStg) model;
            places.addAll(stg.getVisualPlaces());
            Collection<VisualNode> selection = stg.getSelection();
            if (!selection.isEmpty()) {
                places.retainAll(selection);
            }
        }
        return places;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
        if ((model instanceof VisualStg) && (node instanceof VisualStgPlace)) {
            VisualStg stg = (VisualStg) model;
            VisualStgPlace place = (VisualStgPlace) node;
            stg.maybeMakeImplicit(place, true);
        }
    }

}
