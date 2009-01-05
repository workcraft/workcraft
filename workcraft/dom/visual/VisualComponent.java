package org.workcraft.dom.visual;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;

public abstract class VisualComponent extends VisualNode {
	protected Component refComponent = null;
	protected VisualComponentGroup parent;

	public VisualComponent(Component refComponent, VisualComponentGroup parent) {
		super(parent);
		this.refComponent = refComponent;
		this.parent = parent;
	}

	public VisualComponent(Component refComponent, Element xmlElement, VisualComponentGroup parent) {
		super(xmlElement, parent);
		this.refComponent = refComponent;
		this.parent = parent;
	}

	public Component getReferencedComponent() {
		return refComponent;
	}

}
