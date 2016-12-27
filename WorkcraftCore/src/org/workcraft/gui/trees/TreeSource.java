package org.workcraft.gui.trees;

import java.util.List;

import org.workcraft.gui.workspace.Path;

public interface TreeSource<Node> {
    void addListener(TreeListener<Node> listener);
    void removeListener(TreeListener<Node> listener);
    Node getRoot();
    boolean isLeaf(Node node);
    List<Node> getChildren(Node node);
    Path<Node> getPath(Node node);
}
