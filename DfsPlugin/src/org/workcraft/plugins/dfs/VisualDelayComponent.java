package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public abstract class VisualDelayComponent extends VisualComponent {

	public VisualDelayComponent(MathDelayNode ref) {
		super(ref);
		addPropertyDeclarations();
	}

	public MathDelayNode getReferencedDelayComponent() {
		return (MathDelayNode)getReferencedComponent();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualDelayComponent, Double>(
				this, "Delay", Double.class) {
			public void setter(VisualDelayComponent object, Double value) {
				object.getReferencedDelayComponent().setDelay(value);
			}
			public Double getter(VisualDelayComponent object) {
				return object.getReferencedDelayComponent().getDelay();
			}
		});
	}

}
