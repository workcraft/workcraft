package org.workcraft.dom;

import java.awt.geom.AffineTransform;

public abstract class VisualComponent {
	protected AffineTransform transform;

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
