package org.workcraft.gui.tools;

import java.awt.Color;
import java.awt.Graphics2D;

import org.workcraft.plugins.builtin.settings.CommonDecorationSettings;

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
            return CommonDecorationSettings.getShadingColor();
        }
        public static final Shaded INSTANCE = new Shaded();
    }

    class Highlighted implements Decoration {
        @Override
        public Color getColorisation() {
            return CommonDecorationSettings.getHighlightingColor();
        }
        public static final Highlighted INSTANCE = new Highlighted();
    }

    class Selected implements Decoration {
        @Override
        public Color getColorisation() {
            return CommonDecorationSettings.getSelectionColor();
        }
        public static final Selected INSTANCE = new Selected();
    }

}
