package org.workcraft.plugins.circuit.tools;

import org.workcraft.gui.graph.tools.Decoration;

public interface StateDecoration extends Decoration {
    class Empty extends Decoration.Empty implements StateDecoration {
        public static final StateDecoration.Empty INSTANCE = new StateDecoration.Empty();
    }

}
