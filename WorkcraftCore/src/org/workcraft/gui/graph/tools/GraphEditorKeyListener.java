package org.workcraft.gui.graph.tools;

import org.workcraft.gui.events.GraphEditorKeyEvent;

public interface GraphEditorKeyListener {
    boolean keyTyped(GraphEditorKeyEvent event);
    boolean keyPressed(GraphEditorKeyEvent event);
    boolean keyReleased(GraphEditorKeyEvent event);
}
