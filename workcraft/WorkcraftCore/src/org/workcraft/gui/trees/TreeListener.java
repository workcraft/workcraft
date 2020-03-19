package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

public interface TreeListener<T> {
    void added(Path<T> path);
    void removed(Path<T> path);
    void changed(Path<T> path);
    void restructured(Path<T> path);
}
