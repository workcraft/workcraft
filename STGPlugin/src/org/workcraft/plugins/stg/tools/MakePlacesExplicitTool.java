package org.workcraft.plugins.stg.tools;

import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.WorkspaceEntry;

public class MakePlacesExplicitTool extends TransformationTool implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Make places explicit (selected or all)";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return (we.getModelEntry().getMathModel() instanceof STG);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return (node instanceof VisualImplicitPlaceArc);
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualSTG model = (VisualSTG)we.getModelEntry().getVisualModel();
        HashSet<VisualImplicitPlaceArc> connections = new HashSet<>(model.getVisualImplicitPlaceArcs());
        if ( !model.getSelection().isEmpty() ) {
            connections.retainAll(model.getSelection());
        }
        if ( !connections.isEmpty() ) {
            we.saveMemento();
            for (VisualImplicitPlaceArc connection: connections) {
                transform(model, connection);
            }
        }
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualSTG) && (node instanceof VisualImplicitPlaceArc)) {
            VisualImplicitPlaceArc implicitArc = (VisualImplicitPlaceArc)node;
            ((VisualSTG)model).makeExplicit(implicitArc);
        }
    }

}
