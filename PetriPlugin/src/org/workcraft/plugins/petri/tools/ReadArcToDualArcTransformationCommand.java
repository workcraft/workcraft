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
import org.workcraft.util.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ReadArcToDualArcTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {
    private HashSet<VisualConnection> connections = null;

    @Override
    public String getDisplayName() {
        return "Convert read-arcs to dual producing/consuming arcs (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Convert dual producing/consuming arc";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualReadArc;
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
        Collection<Node> readArcs = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            readArcs.addAll(PetriNetUtils.getVisualReadArcs(visualModel));
            Collection<Node> selection = visualModel.getSelection();
            if (!selection.isEmpty()) {
                readArcs.retainAll(selection);
            }
        }
        return readArcs;
    }

    @Override
    public void transform(Model model, Collection<Node> nodes) {
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            connections = new HashSet<>(2 * nodes.size());
            for (Node node: nodes) {
                transform(model, node);
            }
            visualModel.select(new LinkedList<Node>(connections));
            connections.clear();
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualModel) && (node instanceof VisualReadArc)) {
            VisualModel visualModel = (VisualModel) model;
            VisualReadArc readArc = (VisualReadArc) node;
            Pair<VisualConnection, VisualConnection> dualArc = PetriNetUtils.converReadArcTotDualArc(visualModel, readArc);
            VisualConnection consumingArc = dualArc.getFirst();
            if (consumingArc != null) {
                connections.add(consumingArc);
            }
            VisualConnection producingArc = dualArc.getSecond();
            if (producingArc != null) {
                connections.add(producingArc);
            }
        }
    }

}
