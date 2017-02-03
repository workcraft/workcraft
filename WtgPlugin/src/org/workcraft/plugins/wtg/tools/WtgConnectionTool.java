package org.workcraft.plugins.wtg.tools;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.dtd.VisualTransition;
import org.workcraft.plugins.wtg.VisualState;
import org.workcraft.plugins.wtg.VisualWaveform;

public class WtgConnectionTool extends ConnectionTool {

    @Override
    public boolean isConnectable(Node node) {
        return (node instanceof VisualState)
              || (node instanceof VisualWaveform)
              || (node instanceof VisualTransition)
              || (node instanceof VisualSignal);
    }

}
