package org.workcraft.gui.trees;

import javax.swing.Icon;

public interface TreeDecorator<T> {
    String getName(T node);
    Icon getIcon(T node);
}
