package org.workcraft.plugins.wtg.decorations;

import org.workcraft.gui.tools.Decoration;

public interface WaveformDecoration extends Decoration {
    boolean isActive();

    class Active implements WaveformDecoration {
        private Active() {
        }
        @Override
        public boolean isActive() {
            return true;
        }
        public static final Active INSTANCE = new Active();
    }

    class Inactive implements WaveformDecoration {
        private Inactive() {
        }
        @Override
        public boolean isActive() {
            return false;
        }
        public static final Inactive INSTANCE = new Inactive();
    }

}
