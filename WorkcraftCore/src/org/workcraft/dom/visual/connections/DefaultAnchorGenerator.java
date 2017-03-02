package org.workcraft.dom.visual.connections;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.DummyMouseListener;
import org.workcraft.workspace.WorkspaceEntry;

public class DefaultAnchorGenerator extends DummyMouseListener {
    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        if (e.getClickCount() == 2) {
            VisualModel model = e.getModel();
            Node node = HitMan.hitTestCurrentLevelFirst(e.getPosition(), model);
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

}