package org.workcraft.plugins.cpog;

import org.workcraft.dom.math.MathConnection;

public class DynamicVariableConnection extends MathConnection {
    public DynamicVariableConnection() {
    }

    public DynamicVariableConnection(Vertex first, Variable second) {
        super(first, second);
    }
}
