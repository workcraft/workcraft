package org.workcraft.plugins.petri.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DualArcToReadArcTransformationCommand extends AbstractTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Convert dual producing/consuming arcs to read-arcs (selected or all)";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        final VisualModel model = we.getModelEntry().getVisualModel();
        HashSet<Pair<VisualConnection, VisualConnection>> dualArcs = PetriNetUtils.getSelectedOrAllDualArcs(model);
        if (!dualArcs.isEmpty()) {
            we.saveMemento();
            HashSet<VisualReadArc> readArcs = PetriNetUtils.convertDualArcsToReadArcs(model, dualArcs);
            model.select(new LinkedList<Node>(readArcs));
        }
        return null;
    }

    @Override
    public Collection<Node> collect(Model model) {
        return new HashSet<>();
    }

    @Override
    public void transform(Model model, Collection<Node> nodes) {
    }

    @Override
    public void transform(Model model, Node node) {
    }

}
