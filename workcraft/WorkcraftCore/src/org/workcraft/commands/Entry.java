package org.workcraft.commands;

public interface Entry {

    enum Position { TOP, TOP_MIDDLE, MIDDLE, BOTTOM_MIDDLE, BOTTOM }

    String getDisplayName();

    default Position getPosition() {
        return null;
    }

    default int getPriority() {
        return 0;
    }

}
