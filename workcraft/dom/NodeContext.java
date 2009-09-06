package org.workcraft.dom;

import java.util.Set;

public interface NodeContext {
	public Set<Node> getPreset(Node node);
	public Set<Node> getPostset(Node node);
	public Set<Connection> getConnections (Node node);
}
