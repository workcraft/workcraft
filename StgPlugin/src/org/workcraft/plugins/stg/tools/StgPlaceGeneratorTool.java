package org.workcraft.plugins.stg.tools;

import java.awt.Cursor;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class StgPlaceGeneratorTool extends NodeGeneratorTool {

    public StgPlaceGeneratorTool() {
        super(new DefaultNodeGenerator(StgPlace.class));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent event) {
        WorkspaceEntry we = event.getEditor().getWorkspaceEntry();
        VisualNode node = we.getTemplateNode();
        if (node instanceof VisualStgPlace) {
            VisualStgPlace place = (VisualStgPlace) node;
            place.getReferencedComponent().setTokens(event.isMenuKeyDown() ? 1 : 0);
            place.getReferencedComponent().setMutex(event.isShiftKeyDown());
        }
        super.mousePressed(event);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create an empty place. Hold " +
                DesktopApi.getMenuKeyName() +
                " to mark it with a token. Hold Shift to make it mutex.";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (shiftKeyDown) {
            if (menuKeyDown) {
                return GUI.createCursorFromSVG("images/stg-node-mutex-marked.svg");
            } else {
                return GUI.createCursorFromSVG("images/stg-node-mutex-empty.svg");
            }
        } else {
            if (menuKeyDown) {
                return GUI.createCursorFromSVG("images/stg-node-place-marked.svg");
            } else {
                return GUI.createCursorFromSVG("images/stg-node-place-empty.svg");
            }
        }
    }

}

