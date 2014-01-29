package org.workcraft.plugins.dfs;

import java.awt.Color;

import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualAbstractRegister extends VisualDelayComponent {
	protected Color tokenColor = CommonVisualSettings.getBorderColor();

	public VisualAbstractRegister(MathDelayNode ref) {
		super(ref);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualAbstractRegister, Color>(
				this, "Token color", Color.class) {
			public void setter(VisualAbstractRegister object, Color value) {
				object.setTokenColor(value);
			}
			public Color getter(VisualAbstractRegister object) {
				return object.getTokenColor();
			}
		});
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
	}

}
