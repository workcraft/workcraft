package org.workcraft.plugins.circuit.tools;

import org.workcraft.gui.tools.Decoration;

public interface StateDecoration extends Decoration {

    default boolean showForcedInit() {
        return false;
    }

    default boolean useBoldOutline() {
        return false;
    }

}
