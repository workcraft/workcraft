package org.workcraft.dom.math;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;

/**
 * Base type for mathematical objects -- components (graph nodes)
 * and connections (graph arcs).
 * @author Ivan Poliakov
 *
 */
public abstract class MathNode implements Node {
	private String label = "";

	private Node parent = null;

	final public String getLabel() {
		return label;
	}

	final public void setLabel(String label) {
		this.label = label;
	}

	public Collection<Node> getChildren() {
		return new HashSet<Node>();
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}
}