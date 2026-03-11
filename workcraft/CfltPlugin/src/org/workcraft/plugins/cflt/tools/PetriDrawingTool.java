package org.workcraft.plugins.cflt.tools;

import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.cflt.node.NodeCollection;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriDrawingTool extends AbstractDrawingTool {

    public PetriDrawingTool(NodeCollection nodeCollection) {
        super(nodeCollection);
    }

    @Override
    protected AbstractVisualModel getModel(WorkspaceEntry we) {
        return WorkspaceUtils.getAs(we, VisualPetri.class);
    }

    @Override
    protected VisualNode createPlace(
            AbstractVisualModel model,
            boolean hasToken,
            Positioning positioning) {

        VisualPetri visualPetri = (VisualPetri) model;

        VisualPlace place = visualPetri.createPlace(null, null);
        place.getReferencedComponent().setTokens(hasToken ? 1 : 0);
        place.setNamePositioning(positioning);

        return place;
    }

    @Override
    protected VisualNode createTransition(
            AbstractVisualModel model,
            String name) {

        VisualPetri visualPetri = (VisualPetri) model;

        String label = nodeCollection.getNodeDetails(name).getLabel();

        VisualTransition transition = visualPetri.createTransition(null, null);
        transition.setLabel(label);
        transition.setLabelPositioning(Positioning.BOTTOM);
        transition.setNamePositioning(Positioning.LEFT);

        return transition;
    }

    @Override
    public void drawSingleTransition(String name, WorkspaceEntry we) {
        drawSingleTransition(name, we);
    }
}