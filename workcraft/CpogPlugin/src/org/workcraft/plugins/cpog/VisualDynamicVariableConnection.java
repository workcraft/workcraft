package org.workcraft.plugins.cpog;

import org.workcraft.dom.visual.connections.VisualConnection;

public class VisualDynamicVariableConnection extends VisualConnection {

    public VisualDynamicVariableConnection(DynamicVariableConnection mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualDynamicVariableConnection(DynamicVariableConnection mathConnection, VisualVertex first, VisualVariable second) {
        super(mathConnection, first, second);
    }

    @Override
    public boolean hasArrow() {
        return false;
    }
}
