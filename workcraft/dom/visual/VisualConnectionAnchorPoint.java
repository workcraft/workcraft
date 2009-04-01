package org.workcraft.dom.visual;


public abstract class VisualConnectionAnchorPoint extends VisualTransformableNode {
	VisualConnection parentConnection;
	public VisualConnectionAnchorPoint(VisualConnection parent) {
		parentConnection = parent;
	}
	public VisualConnection getParentConnection() {
		return parentConnection;
	}
}
