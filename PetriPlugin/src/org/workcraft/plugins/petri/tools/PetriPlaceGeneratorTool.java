package org.workcraft.plugins.petri.tools;

import java.awt.Cursor;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.GUI;

public class PetriPlaceGeneratorTool extends NodeGeneratorTool {

    public PetriPlaceGeneratorTool() {
        super(new DefaultNodeGenerator(Place.class));
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        VisualNode node = e.getModel().getTemplateNode();
        if (node instanceof VisualPlace) {
            VisualPlace place = (VisualPlace) node;
            place.getReferencedPlace().setTokens(e.isMenuKeyDown() ? 1 : 0);
        }
        super.mousePressed(e);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create an empty place (hold " +
                DesktopApi.getMenuKeyMaskName() +
                "to mark it with a token).";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (menuKeyDown) {
            return GUI.createCursorFromSVG("images/petri-node-place-marked.svg");
        } else {
            return GUI.createCursorFromSVG("images/petri-node-place-empty.svg");
        }
    }

}

