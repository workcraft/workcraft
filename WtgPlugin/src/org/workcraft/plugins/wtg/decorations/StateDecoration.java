package org.workcraft.plugins.wtg.decorations;

import org.workcraft.gui.tools.Decoration;

public interface StateDecoration extends Decoration {
    boolean isMarked();

    class Marked extends Empty implements StateDecoration {
        @Override
        public boolean isMarked() {
            return true;
        }
        public static final Marked INSTANCE = new Marked();
    }

    class Unmarked extends Empty implements StateDecoration {
        @Override
        public boolean isMarked() {
            return false;
        }
        public static final Unmarked INSTANCE = new Unmarked();
    }

}
