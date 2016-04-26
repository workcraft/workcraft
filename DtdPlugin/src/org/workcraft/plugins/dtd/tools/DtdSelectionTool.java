package org.workcraft.plugins.dtd.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.dtd.DtdUtils;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;

public class DtdSelectionTool extends SelectionTool {

    public DtdSelectionTool() {
        super(false);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualDtd model = (VisualDtd) e.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            Node node = HitMan.hitTestForSelection(e.getPosition(), model);
            if ((node instanceof VisualSignal) && (e.getClickCount() > 1)) {
                VisualSignal signal = (VisualSignal) node;
                processed = model.appendSignalEvent(signal).isValid();
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

}
