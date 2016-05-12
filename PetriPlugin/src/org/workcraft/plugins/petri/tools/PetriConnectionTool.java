package org.workcraft.plugins.petri.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;

public class PetriConnectionTool extends ConnectionTool {

    public PetriConnectionTool() {
        super(true, true, true);
    }

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualPlace)
              || (node instanceof VisualReplicaPlace)
              || (node instanceof VisualTransition);
    }

    @Override
    public String getSecondHintMessage() {
        return super.getSecondHintMessage() + " Hold Shift to create a place proxy.";
    }

    @Override
    public VisualConnection finishConnection(GraphEditorMouseEvent e) {
        VisualConnection connection = super.finishConnection(e);
        if (connection != null) {
            if ((connection.getFirst() instanceof VisualPlace)
                    || (connection.getSecond() instanceof VisualPlace)) {

                if ((e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
                    VisualModel visualModel = e.getEditor().getModel();
                    connection = PetriNetUtils.replicateConnectedPlace(visualModel, connection);
                }
            }
        }
        return connection;
    }

}
