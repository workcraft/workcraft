package org.workcraft.gui.tools;

import org.workcraft.gui.events.GraphEditorKeyEvent;

public interface GraphEditorKeyListener {
    boolean keyTyped(GraphEditorKeyEvent event);
    boolean keyPressed(GraphEditorKeyEvent event);
    boolean keyReleased(GraphEditorKeyEvent event);
}
