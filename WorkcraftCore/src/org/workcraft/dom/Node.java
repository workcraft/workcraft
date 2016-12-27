package org.workcraft.dom;
import java.util.Collection;

public interface Node {
    Node getParent();
    void setParent(Node parent);
    Collection<Node> getChildren();
}
