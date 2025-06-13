package org.workcraft.plugins.petri.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.ConnectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.utils.ConversionUtils;
import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class PetriReadArcConnectionTool extends ConnectionTool {

    public PetriReadArcConnectionTool() {
        super(true, false, true);
    }

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualPlace)
                || (node instanceof VisualReplicaPlace)
                || (node instanceof VisualTransition);
    }

    @Override
    public VisualConnection createTemplateNode() {
        return new VisualReadArc();
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/petri-tool-readarc.svg");
    }

    @Override
    public String getLabel() {
        return "Read-arc";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_R;
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

                if (e.isShiftKeyDown()) {
                    VisualModel visualModel = e.getEditor().getModel();
                    connection = ConversionUtils.replicateConnectedPlace(visualModel, connection);
                }
            }
        }
        return connection;
    }

}
