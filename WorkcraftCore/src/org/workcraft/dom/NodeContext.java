package org.workcraft.dom;

import java.util.Set;

public interface NodeContext {
    Set<Node> getPreset(Node node);
    Set<Node> getPostset(Node node);
    Set<Connection> getConnections(Node node);
    boolean hasConnection(Node first, Node second);
    Connection getConnection(Node first, Node second);
}
