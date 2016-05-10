package org.workcraft.plugins.dtd.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.dtd.DtdUtils;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;

public class DtdSelectionTool extends SelectionTool {

    public DtdSelectionTool() {
        super(false, false, false, false);
    }

    @Override
    public void activated(GraphEditor editor) {
        super.activated(editor);
        editor.getWorkspaceEntry().setCanCopy(false);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualDtd model = (VisualDtd) e.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            Node node = HitMan.hitTestForSelection(e.getPosition(), model);
            if ((node instanceof VisualSignal) && (e.getClickCount() > 1)) {
                VisualSignal signal = (VisualSignal) node;
                processed = model.appendSignalEvent(signal, null).isValid();
            }
            if ((node instanceof VisualConnection) && (e.getClickCount() > 1)) {
                VisualConnection connection = (VisualConnection) node;
                if (DtdUtils.isLevelConnection(connection)) {
                    processed = model.insetrSignalPulse(connection).isValid();
                }
            }
        }

        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public void beforeSelectionModification(final GraphEditor editor) {
        super.beforeSelectionModification(editor);
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            for (Node node : model.getSelection()) {
                VisualConnection connection = null;
                if (node instanceof VisualConnection) {
                    connection = (VisualConnection) node;
                } else if (node instanceof ControlPoint) {
                    connection = ConnectionHelper.getParentConnection((ControlPoint) node);
                }
                if (DtdUtils.isLevelConnection(connection)) {
                    model.removeFromSelection(node);
                }
            }
        }
    }

}
