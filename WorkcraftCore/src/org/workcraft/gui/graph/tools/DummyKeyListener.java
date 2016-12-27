package org.workcraft.gui.graph.tools;

import org.workcraft.gui.events.GraphEditorKeyEvent;

public class DummyKeyListener implements GraphEditorKeyListener {

    private static DummyKeyListener instance = new DummyKeyListener();

    public static DummyKeyListener getInstance() {
        return instance;
    }

    public boolean keyPressed(GraphEditorKeyEvent event) {
        return false;
    }

    public boolean keyReleased(GraphEditorKeyEvent event) {
        return false;
    }

    public boolean keyTyped(GraphEditorKeyEvent event) {
        return false;
    }

}
