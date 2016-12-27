package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

public interface TreeListener<Node> {
    void added(Path<Node> path);
    void removed(Path<Node> path);
    void changed(Path<Node> path);
    void restructured(Path<Node> path);
}
