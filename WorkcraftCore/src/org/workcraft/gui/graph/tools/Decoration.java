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
        public static final Empty INSTANCE = new Empty();
    }

    class Shaded implements Decoration {
        @Override
        public Color getColorisation() {
            return Color.LIGHT_GRAY;
        }
        @Override
        public Color getBackground() {
            return null;
        }
        public static final Shaded INSTANCE = new Shaded();
    }

    class Highlighted implements Decoration {
        @Override
        public Color getColorisation() {
            return new Color(1.0f, 0.5f, 0.0f).brighter();
        }
        @Override
        public Color getBackground() {
            return null;
        }
        public static final Highlighted INSTANCE = new Highlighted();
    }

    class Selected implements Decoration {
        @Override
        public Color getColorisation() {
            return new Color(99, 130, 191).brighter();
        }
        @Override
        public Color getBackground() {
            return null;
        }
        public static final Selected INSTANCE = new Selected();
    }

}
