package org.workcraft.dom.visual;

import java.awt.geom.Point2D;

import org.workcraft.dom.Component;

public abstract class VisualComponent extends VisualNode {
	protected Component refComponent = null;

	public VisualComponent(Component refComponent) {
		this.refComponent = refComponent;
	}

	public Component getReferencedComponent() {
		return refComponent;
	}


	public boolean hitTest(Point2D point) {
		return getBoundingBox().contains(point);
	}

}
