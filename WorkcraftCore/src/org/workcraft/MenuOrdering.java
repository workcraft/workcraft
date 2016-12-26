package org.workcraft;

public interface MenuOrdering {
    enum Position { TOP, MIDDLE, BOTTOM };
    int getPriority();
    Position getPosition();
}
