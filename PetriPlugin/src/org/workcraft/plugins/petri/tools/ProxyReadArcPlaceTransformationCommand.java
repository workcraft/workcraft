package org.workcraft.plugins.petri.tools;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.AbstractTransformationCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.workspace.ModelEntry;

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
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof PetriNetModel;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        if (node instanceof VisualReadArc) {
            VisualReadArc readArc = (VisualReadArc) node;
            return readArc.getFirst() instanceof VisualPlace;
        }
        return false;
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
        Collection<Node> readArcs = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            readArcs.addAll(PetriNetUtils.getVisualReadArcs(visualModel));
            Collection<Node> selection = visualModel.getSelection();
            if (!selection.isEmpty()) {
                readArcs.retainAll(selection);
            }
            HashSet<VisualPlace> places = PetriNetUtils.getVisualPlaces(visualModel);
            if (!selection.isEmpty()) {
                places.retainAll(selection);
            }
            for (VisualPlace place: places) {
                for (Connection connection: model.getConnections(place)) {
                    if (connection instanceof VisualReadArc) {
                        readArcs.add(connection);
                    }
                }
            }
        }
        return readArcs;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualReadArc)) {
            VisualModel visualModel = (VisualModel) model;
            VisualReadArc readArc = (VisualReadArc) node;
            PetriNetUtils.replicateConnectedPlace(visualModel, readArc);
        }
    }

}
