package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.Graphics2D;

import org.workcraft.plugins.shared.CommonDecorationSettings;

public interface Decoration {
    default Color getColorisation() {
        return null;
    }

    default Color getBackground() {
        return null;
    }

    default void decorate(Graphics2D g) {
    }

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
        public static final Shaded INSTANCE = new Shaded();
    }

    class Highlighted implements Decoration {
        @Override
        public Color getColorisation() {
            return CommonDecorationSettings.getHighlightedComponentColor();
        }
        public static final Highlighted INSTANCE = new Highlighted();
    }

    class Selected implements Decoration {
        @Override
        public Color getColorisation() {
            return CommonDecorationSettings.getSelectedComponentColor();
        }
        public static final Selected INSTANCE = new Selected();
    }

}
