package org.workcraft.gui.trees;

import javax.swing.Icon;

public interface TreeDecorator<Node> {
    String getName(Node node);
    Icon getIcon(Node node);
}
