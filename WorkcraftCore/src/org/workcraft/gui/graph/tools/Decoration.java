package org.workcraft.gui.graph.tools;

import java.awt.Color;

import org.workcraft.plugins.shared.CommonDecorationSettings;

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
            return CommonDecorationSettings.getShadedComponentColor();
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
            return CommonDecorationSettings.getHighlightedComponentColor();
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
            return CommonDecorationSettings.getSelectedComponentColor();
        }
        @Override
        public Color getBackground() {
            return null;
        }
        public static final Selected INSTANCE = new Selected();
    }

}
