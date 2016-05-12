package org.workcraft.plugins.petri.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class ReadArcToDualArcConverterTool extends TransformationTool implements NodeTransformer {
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
        return we.getModelEntry().getMathModel() instanceof PetriNetModel;
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualReadArc;
    }

    @Override
    public boolean isEnabled(WorkspaceEntry we, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualModel visualModel = we.getModelEntry().getVisualModel();
        HashSet<VisualReadArc> readArcs = PetriNetUtils.getVisualReadArcs(visualModel);
        if (!visualModel.getSelection().isEmpty()) {
            readArcs.retainAll(visualModel.getSelection());
        }
        if (!readArcs.isEmpty()) {
            we.saveMemento();
            connections = new HashSet<>(2 * readArcs.size());
            for (VisualReadArc readArc: readArcs) {
                transform(visualModel, readArc);
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
