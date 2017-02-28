package org.workcraft.plugins.wtg.tools;

import org.workcraft.dom.Node;
import org.workcraft.plugins.dtd.tools.DtdConnectionTool;
import org.workcraft.plugins.wtg.VisualState;
import org.workcraft.plugins.wtg.VisualWaveform;

public class WtgConnectionTool extends DtdConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        return super.isConnectable(node)
              || (node instanceof VisualState)
              || (node instanceof VisualWaveform);
    }

}
