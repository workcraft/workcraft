package org.workcraft.plugins.petri.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.AbstractTransformationCommand;
import org.workcraft.NodeTransformer;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DirectedArcToReadArcTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private HashSet<VisualReadArc> readArcs = null;

    @Override
    public String getDisplayName() {
        return "Convert selected arcs to read-arcs";
    }

    @Override
    public String getPopupName() {
        return "Convert to read-arc";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return PetriNetUtils.isVisualConsumingArc(node) || PetriNetUtils.isVisualProducingArc(node);
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> arcs = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            arcs.addAll(PetriNetUtils.getVisualConsumingArcs(visualModel));
            arcs.addAll(PetriNetUtils.getVisualProducingArcs(visualModel));
            Collection<Node> selection = visualModel.getSelection();
            arcs.retainAll(selection);
        }
        return arcs;
    }

    @Override
    public void transform(Model model, Collection<Node> nodes) {
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            readArcs = new HashSet<>();
            for (Node node: nodes) {
                // Check that the arc was not removed because of a dual arc
                if (node.getParent() != null) {
                    transform(model, node);
                }
            }
            visualModel.select(new LinkedList<Node>(readArcs));
            readArcs = null;
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualConnection)) {
            VisualConnection connection = (VisualConnection) node;
            VisualReadArc readArc = PetriNetUtils.convertDirectedArcToReadArc((VisualModel) model, connection);
            if ((readArcs != null) && (readArc != null)) {
                readArcs.add(readArc);
            }
        }
    }

}
