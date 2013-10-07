package org.workcraft.plugins.policy;

import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;

@VisualClass(org.workcraft.plugins.policy.VisualLocality.class)
public class Locality extends MathGroup {

	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
	}

	@Override
	public void add(Node node) {
		super.add(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		super.add(nodes);
	}

	@Override
	public void remove(Node node) {
		super.remove(node);
	}

	@Override
	public void remove(Collection<Node> nodes) {
		super.remove(nodes);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		super.reparent(nodes, newParent);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		super.reparent(nodes);
	}

}
