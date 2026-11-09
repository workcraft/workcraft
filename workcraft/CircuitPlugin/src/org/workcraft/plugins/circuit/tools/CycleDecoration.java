package org.workcraft.plugins.circuit.tools;

import org.workcraft.gui.tools.Decoration;

import java.awt.*;

public class CycleDecoration implements Decoration {

    private final Color color;

    public CycleDecoration(Color color) {
        this.color = color;
    }

    @Override
    public Color getColorisation() {
        return color;
    }

    @Override
    public Color getBackground() {
        return color;
    }

}
