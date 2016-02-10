package org.workcraft.gui.graph.tools;

import java.awt.Color;

public interface Decoration {
    Color getColorisation();
    Color getBackground();
    class Empty implements Decoration {

        @Override
        public Color getColorisation() {
            return null;
        }

        @Override
        public Color getBackground() {
            return null;
        }

        public static Empty INSTANCE = new Empty();
    }
}
