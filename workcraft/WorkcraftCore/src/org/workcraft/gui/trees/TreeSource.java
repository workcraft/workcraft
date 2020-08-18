package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

import java.util.List;

public interface TreeSource<T> {
    void addListener(TreeListener<T> listener);
    void removeListener(TreeListener<T> listener);
    T getRoot();
    boolean isLeaf(T node);
    List<T> getChildren(T node);
    Path<T> getPath(T node);
}
