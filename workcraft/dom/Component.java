package org.workcraft.dom;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Component extends MathNode {
	private Set<Connection> connections = new LinkedHashSet<Connection>();
	private Set<Component> preset = new LinkedHashSet<Component>();
	private Set<Component> postset = new LinkedHashSet<Component>();

	final public void addToPreset (Component component) {
		preset.add(component);
	}

	final public void removeFromPreset(Component component) {
		preset.remove(component);
	}

	final public void addToPostset (Component component) {
		postset.add(component);
	}
	final public void removeFromPostset(Component component) {
		postset.remove(component);
	}

	final public void addConnection(Connection connection) {
		connections.add(connection);
	}

	final public void removeConnection(Connection connection) {
		connections.remove(connection);
	}

	final public Set<Connection> getConnections() {
		return new HashSet<Connection>(connections);
	}

	final public Set<Component> getPreset() {
		return new HashSet<Component>(preset);
	}

	final public Set<Component> getPostset() {
		return new HashSet<Component>(postset);
	}
}