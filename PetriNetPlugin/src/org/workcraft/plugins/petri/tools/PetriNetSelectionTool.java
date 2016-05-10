package org.workcraft.plugins.petri.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;

public class PetriNetSelectionTool extends SelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualModel model = e.getEditor().getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
            if (node instanceof VisualPlace) {
                VisualPlace place = (VisualPlace) node;
                if (place.getReferencedPlace().getTokens() <= 1) {
                    e.getEditor().getWorkspaceEntry().saveMemento();

                    if (place.getReferencedPlace().getTokens() == 1) {
                        place.getReferencedPlace().setTokens(0);
                    } else {
                        place.getReferencedPlace().setTokens(1);
                    }
                }
                processed = true;
            }
        }

        if (!processed) {
            super.mouseClicked(e);
        }
    }

}
