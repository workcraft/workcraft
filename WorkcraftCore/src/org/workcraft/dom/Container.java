package org.workcraft.dom;

import java.util.Collection;

public interface Container extends Node {

    void add(Node node);
    void add(Collection<? extends Node> nodes);

    void remove(Node node);
    void remove(Collection<? extends Node> nodes);

    void reparent(Collection<? extends Node> nodes);
    void reparent(Collection<? extends Node> nodes, Container newParent);

}
