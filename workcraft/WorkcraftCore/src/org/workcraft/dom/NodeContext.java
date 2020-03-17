package org.workcraft.dom;

import java.util.Set;

public interface NodeContext<N extends Node, C extends Connection> {
    Set<N> getPreset(N node);
    Set<N> getPostset(N node);
    Set<C> getConnections(N node);
    boolean hasConnection(N first, N second);
    C getConnection(N first, N second);
}
