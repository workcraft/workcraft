package org.workcraft.plugins.petri.tools;

import java.awt.Cursor;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PlaceGeneratorTool extends NodeGeneratorTool {

    public PlaceGeneratorTool() {
        super(new DefaultNodeGenerator(Place.class));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent event) {
        WorkspaceEntry we = event.getEditor().getWorkspaceEntry();
        VisualNode node = we.getTemplateNode();
        if (node instanceof VisualPlace place) {
            place.getReferencedComponent().setTokens(event.isMenuKeyDown() ? 1 : 0);
        }
        super.mousePressed(event);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create an empty place (hold " +
                DesktopApi.getMenuKeyName() +
                " to mark it with a token).";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (menuKeyDown) {
            return GuiUtils.createCursorFromSVG("images/petri-node-place-marked.svg");
        } else {
            return GuiUtils.createCursorFromSVG("images/petri-node-place-empty.svg");
        }
    }

}

