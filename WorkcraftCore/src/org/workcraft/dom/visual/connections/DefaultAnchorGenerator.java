package org.workcraft.dom.visual.connections;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditorMouseListener;
import org.workcraft.workspace.WorkspaceEntry;

public class DefaultAnchorGenerator implements GraphEditorMouseListener {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        if (e.getClickCount() == 2) {
            VisualModel model = e.getModel();
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualConnection) {
                VisualConnection connection = (VisualConnection) node;
                WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
                we.captureMemento();
                ControlPoint cp = ConnectionHelper.createControlPoint(connection, e.getPosition());
                if (cp == null) {
                    we.cancelMemento();
                } else {
                    we.saveMemento();
                    model.select(cp);
                }
            }
        }
    }

    public void mouseEntered(GraphEditorMouseEvent e) {
    }

    public void mouseExited(GraphEditorMouseEvent e) {
    }

    public void mouseMoved(GraphEditorMouseEvent e) {
    }

    public void mousePressed(GraphEditorMouseEvent e) {
    }

    public void mouseReleased(GraphEditorMouseEvent e) {
    }

    public void startDrag(GraphEditorMouseEvent e) {
    }

    public void finishDrag(GraphEditorMouseEvent e) {
    }

    public boolean isDragging() {
        return false;
    }

}