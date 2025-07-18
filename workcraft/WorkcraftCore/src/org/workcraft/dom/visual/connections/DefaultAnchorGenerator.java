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
            if (node instanceof VisualConnection connection) {
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

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
    }

    @Override
    public void mouseReleased(GraphEditorMouseEvent e) {
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
    }

    @Override
    public void finishDrag(GraphEditorMouseEvent e) {
    }

    @Override
    public boolean isDragging() {
        return false;
    }

}