package org.workcraft.gui.editor;

import java.awt.*;

public interface Overlay {
    Component add(Component component);
    void remove(Component component);
}
