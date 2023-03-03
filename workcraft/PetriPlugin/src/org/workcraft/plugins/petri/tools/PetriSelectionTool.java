package org.workcraft.plugins.petri.tools;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.utils.DesktopApi;

import java.awt.event.MouseEvent;

public class PetriSelectionTool extends SelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualModel model = e.getEditor().getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualPlace) {
                if (e.isMenuKeyDown()) {
                    VisualPlace place = (VisualPlace) node;
                    if (place.getReferencedComponent().getTokens() <= 1) {
                        e.getEditor().getWorkspaceEntry().saveMemento();

                        if (place.getReferencedComponent().getTokens() == 1) {
                            place.getReferencedComponent().setTokens(0);
                        } else {
                            place.getReferencedComponent().setTokens(1);
                        }
                    }
                }
                processed = true;
            }
        }

        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        if (getDragState() == DragState.NONE) {
            if (getCurrentNode() instanceof VisualPlace) {
                return DesktopApi.getMenuKeyName() + "+double-click to toggle place marking.";
            }
        }
        return super.getHintText(editor);
    }

}
