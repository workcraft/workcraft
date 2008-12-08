package org.workcraft.dom.visual;

import java.awt.geom.Point2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;

public abstract class VisualComponent extends VisualNode {
	Component refComponent = null;
	VisualNode parent;

	public VisualComponent(Component refComponent) {
		this.refComponent = refComponent;
	}

	public VisualComponent(Component refComponent, Element xmlElement) {
		super(xmlElement);
		this.refComponent = refComponent;
	}

	public Component getReferencedComponent() {
		return this.refComponent;
	}


	public boolean hitTest(Point2D point) {
		return getBoundingBox().contains(point);
	}

	public VisualNode getParent() {
		return parent;
	}

}
