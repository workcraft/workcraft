package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Set;

import org.workcraft.dom.Component;

public abstract class VisualComponent extends VisualNode {
	protected Component refComponent = null;

	public VisualComponent(Component refComponent) {
		this.refComponent = refComponent;
	}

	public Component getReferencedComponent() {
		return refComponent;
	}


}
