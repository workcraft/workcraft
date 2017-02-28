package org.workcraft.plugins.dtd.tools;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.dtd.VisualSignalEvent;

public class DtdConnectionTool extends ConnectionTool {

    public DtdConnectionTool() {
        super(true, true, false, false);
    }

    @Override
    public boolean isConnectable(Node node) {
        return node instanceof VisualSignalEvent;
    }

}
