package org.workcraft.plugins.stg.tools;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Replica;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.WorkspaceEntry;

public class MakePlacesImplicitTool extends TransformationTool implements NodeTransformer {

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
        return we.getModelEntry().getMathModel() instanceof Stg;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualPlace;
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
        if (node instanceof VisualPlace) {
            VisualModel model = we.getModelEntry().getVisualModel();
            VisualPlace place = (VisualPlace) node;
            Collection<Node> preset = model.getPreset(place);
            Collection<Node> postset = model.getPostset(place);
            Collection<Replica> replicas = place.getReplicas();
            if ((preset.size() == 1) && (postset.size() == 1) && replicas.isEmpty()) {
                VisualComponent first = (VisualComponent) preset.iterator().next();
                VisualComponent second = (VisualComponent) postset.iterator().next();
                if (!PetriNetUtils.hasImplicitPlaceArcConnection(model, first, second)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualStg model = (VisualStg) we.getModelEntry().getVisualModel();
        HashSet<VisualPlace> places = new HashSet<>(model.getVisualPlaces());
        if (!model.getSelection().isEmpty()) {
            places.retainAll(model.getSelection());
        }
        if (!places.isEmpty()) {
            we.saveMemento();
            for (VisualPlace place: places) {
                transform(model, place);
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualStg) && (node instanceof VisualPlace)) {
            VisualPlace place = (VisualPlace) node;
            ((VisualStg) model).maybeMakeImplicit(place, true);
        }
    }

}
