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
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.VisualStg;

public class StgSelectionTool extends SelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            GraphEditor editor = e.getEditor();
            VisualModel model = editor.getModel();
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node != null) {
                if (node instanceof VisualPlace) {
                    Place place = ((VisualPlace) node).getReferencedPlace();
                    toggleToken(place, editor);
                    return;
                }
                if (node instanceof VisualImplicitPlaceArc) {
                    if ((e.getKeyModifiers() & DesktopApi.getMenuKeyMouseMask()) != 0) {
                        Place place = ((VisualImplicitPlaceArc) node).getImplicitPlace();
                        toggleToken(place, editor);
                        return;
                    }
                }
                if (node instanceof VisualSignalTransition) {
                    VisualSignalTransition transition = (VisualSignalTransition) node;
                    boolean processed = false;
                    if ((e.getKeyModifiers() & DesktopApi.getMenuKeyMouseMask()) != 0) {
                        toggleSignalType(transition.getReferencedTransition(), editor);
                        processed = true;
                    }
                    if ((e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
                        toggleDirection(transition.getReferencedTransition(), editor);
                        processed = true;
                    }
                    if (processed) {
                        return;
                    }
                }
                if (node instanceof VisualNamedTransition) {
                    VisualNamedTransition transition = (VisualNamedTransition) node;
                    AbstractInplaceEditor textEditor = new NameInplaceEditor(editor, transition);
                    textEditor.edit(transition.getName(), transition.getNameFont(),
                            transition.getNameOffset(), Alignment.CENTER, false);
                    return;
                }
            }
        }
        super.mouseClicked(e);
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

    private void toggleSignalType(SignalTransition transition, GraphEditor editor) {
        editor.getWorkspaceEntry().saveMemento();
        Signal.Type type = transition.getSignalType();
        transition.setSignalType(type.mirror());
    }

    private void toggleDirection(SignalTransition transition, GraphEditor editor) {
        editor.getWorkspaceEntry().saveMemento();
        SignalTransition.Direction direction = transition.getDirection();
        VisualModel model = editor.getModel();
        if (model instanceof VisualStg) {
            Stg stg = (Stg) model.getMathModel();
            stg.setDirection(transition, direction.mirror());
        }
    }

}
