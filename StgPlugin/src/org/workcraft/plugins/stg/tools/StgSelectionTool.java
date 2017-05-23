package org.workcraft.plugins.stg.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Alignment;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.editors.AbstractInplaceEditor;
import org.workcraft.gui.graph.editors.NameInplaceEditor;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualNamedTransition;

public class StgSelectionTool extends SelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            GraphEditor editor = e.getEditor();
            VisualModel model = editor.getModel();
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node != null) {
                if (node instanceof VisualPlace) {
                    Place place = ((VisualPlace) node).getReferencedPlace();
                    toggleToken(place, editor);
                    processed = true;
                } else if (node instanceof VisualImplicitPlaceArc) {
                    if (e.getKeyModifiers() == DesktopApi.getMenuKeyMouseMask()) {
                        Place place = ((VisualImplicitPlaceArc) node).getImplicitPlace();
                        toggleToken(place, editor);
                        processed = true;
                    }
                } else if (node instanceof VisualNamedTransition) {
                    final VisualNamedTransition transition = (VisualNamedTransition) node;
                    AbstractInplaceEditor textEditor = new NameInplaceEditor(editor, transition);
                    textEditor.edit(transition.getName(), transition.getNameFont(),
                            transition.getNameOffset(), Alignment.CENTER, false);
                    processed = true;
                }
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    private void toggleToken(Place place, GraphEditor editor) {
        if (place.getTokens() <= 1) {
            editor.getWorkspaceEntry().saveMemento();

            if (place.getTokens() == 1) {
                place.setTokens(0);
            } else {
                place.setTokens(1);
            }
        }
    }


}
