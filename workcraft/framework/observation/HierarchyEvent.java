package org.workcraft.framework.observation;

import java.util.Collection;

import org.workcraft.dom.Node;

public interface HierarchyEvent {
	public Object getSender();
	public Collection<Node> getAffectedNodes();
}