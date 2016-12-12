package org.workcraft.plugins.petri.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.util.Pair;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class DualArcToReadArcConverterTool extends TransformationTool {

    @Override
    public String getDisplayName() {
        return "Convert dual producing/consuming arcs to read-arcs (selected or all)";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, PetriNetModel.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final VisualModel model = me.getVisualModel();
        HashSet<Pair<VisualConnection, VisualConnection>> dualArcs = PetriNetUtils.getSelectedOrAllDualArcs(model);
        if (!dualArcs.isEmpty()) {
            final Framework framework = Framework.getInstance();
            framework.saveMementoInCurrentWorkspaceEntry();
            HashSet<VisualReadArc> readArcs = PetriNetUtils.convertDualArcsToReadArcs(model, dualArcs);
            model.select(new LinkedList<Node>(readArcs));
        }
        return me;
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
