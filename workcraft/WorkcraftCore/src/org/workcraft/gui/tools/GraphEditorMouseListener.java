package org.workcraft.gui.tools;

import org.workcraft.gui.events.GraphEditorMouseEvent;

public interface GraphEditorMouseListener {
    void mouseMoved(GraphEditorMouseEvent e);
    void mouseClicked(GraphEditorMouseEvent e);
    void mousePressed(GraphEditorMouseEvent e);
    void mouseReleased(GraphEditorMouseEvent e);
    void startDrag(GraphEditorMouseEvent e);
    void finishDrag(GraphEditorMouseEvent e);
    boolean isDragging();
}
