package org.workcraft.plugins.dtd.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.dtd.VisualEvent;

public class DtdConnectionTool extends ConnectionTool {

    public DtdConnectionTool() {
        super(true, true, false);
    }

    @Override
    public boolean isConnectable(Node node) {
        return node instanceof VisualEvent;
    }

    @Override
    public VisualConnection createTemplateNode() {
        return null;
    }

}
