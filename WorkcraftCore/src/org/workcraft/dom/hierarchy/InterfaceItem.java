package org.workcraft.dom.hierarchy;

import java.util.HashSet;

import org.workcraft.dom.Node;

public class InterfaceItem {
    HashSet<Node> relatedNodes = new HashSet<>();

    public HashSet<Node> getRelatedNodes() {
        return relatedNodes;
    }
}
