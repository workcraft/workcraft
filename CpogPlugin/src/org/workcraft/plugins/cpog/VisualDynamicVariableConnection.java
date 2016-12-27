package org.workcraft.plugins.cpog;

import org.workcraft.dom.visual.connections.VisualConnection;

public class VisualDynamicVariableConnection extends VisualConnection {
    DynamicVariableConnection mathConnection;

    public VisualDynamicVariableConnection(DynamicVariableConnection mathConnection) {
        super();
        this.mathConnection = mathConnection;
    }

    public VisualDynamicVariableConnection(DynamicVariableConnection mathConnection, VisualVertex first, VisualVariable second) {
        super(mathConnection, first, second);
        this.mathConnection = mathConnection;
    }

    @Override
    public boolean hasArrow() {
        return false;
    }
}
