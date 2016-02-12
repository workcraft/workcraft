package org.workcraft.plugins.stg.tools;

import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class MakePlacesImplicitTool extends TransformationTool implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Make places implicit (selected or all)";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof STG;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualPlace;
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualSTG model = (VisualSTG) we.getModelEntry().getVisualModel();
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
        if ((model instanceof VisualSTG) && (node instanceof VisualPlace)) {
            VisualPlace place = (VisualPlace) node;
            ((VisualSTG) model).maybeMakeImplicit(place, true);
        }
    }

}
