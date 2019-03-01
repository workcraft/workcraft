package org.workcraft.plugins.stg.tools;

import java.awt.Cursor;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualStgPlace;
import org.workcraft.utils.GuiUtils;
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
                return GuiUtils.createCursorFromSVG("images/stg-node-mutex-marked.svg");
            } else {
                return GuiUtils.createCursorFromSVG("images/stg-node-mutex-empty.svg");
            }
        } else {
            if (menuKeyDown) {
                return GuiUtils.createCursorFromSVG("images/stg-node-place-marked.svg");
            } else {
                return GuiUtils.createCursorFromSVG("images/stg-node-place-empty.svg");
            }
        }
    }

}

