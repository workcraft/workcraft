package org.workcraft.observation;

import java.util.Collection;

import org.workcraft.dom.Node;

public interface HierarchyEvent {
    Object getSender();
    Collection<Node> getAffectedNodes();
}
