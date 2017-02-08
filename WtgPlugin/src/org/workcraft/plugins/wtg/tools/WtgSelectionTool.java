package org.workcraft.plugins.wtg.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.dtd.tools.DtdSelectionTool;
import org.workcraft.plugins.wtg.VisualState;
import org.workcraft.plugins.wtg.VisualWtg;
import org.workcraft.workspace.WorkspaceEntry;

public class WtgSelectionTool extends DtdSelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
        VisualWtg model = (VisualWtg) e.getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            Node node = HitMan.hitTestCurrentLevelFirst(e.getPosition(), model);
            if (node instanceof VisualState) {
                we.saveMemento();
                VisualState state = (VisualState) node;
                boolean isInitial = state.getReferencedState().isInitial();
                state.getReferencedState().setInitial(!isInitial);
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (model.getCurrentLevel() == model.getRoot()) {
            return "Double-click on a waveform to edit it. Double-click on a nodal state to toggle its marking.";
        }
        return super.getHintText(editor);
    }

}
