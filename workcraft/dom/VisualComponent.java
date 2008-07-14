package org.workcraft.dom;

import java.awt.geom.AffineTransform;
import java.util.Set;

public abstract class VisualComponent {
	protected AffineTransform transform;
	protected VisualComponent parent;
	protected Component refComponent;
	protected Set<VisualComponent> children;

	public VisualComponent() {

	}

	public double getX() {
		return transform.getTranslateX();
	}

	public double getY() {
		return transform.getTranslateY();
	}

	public void setX(double x) {
		transform.translate(x-transform.getTranslateX(), 0);
	}

	public void setY(double y) {
		transform.translate(0, y - transform.getTranslateY());
	}
}
