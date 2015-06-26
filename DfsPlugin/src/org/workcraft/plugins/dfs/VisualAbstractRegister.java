package org.workcraft.plugins.dfs;

import java.awt.Color;

import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualAbstractRegister extends VisualDelayComponent {
	public static final String PROPERTY_TOKEN_COLOR = "Token color";
	protected Color tokenColor = CommonVisualSettings.getBorderColor();

	public VisualAbstractRegister(MathDelayNode ref) {
		super(ref);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualAbstractRegister, Color>(
				this, PROPERTY_TOKEN_COLOR, Color.class) {
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
		sendNotification(new PropertyChangedEvent(this, PROPERTY_TOKEN_COLOR));
	}

	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualAbstractRegister) {
			VisualAbstractRegister srcComponent = (VisualAbstractRegister)src;
			setTokenColor(srcComponent.getTokenColor());
		}
	}

}
