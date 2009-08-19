package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.Coloriser;


public abstract class VisualConnectionAnchorPoint extends VisualTransformableNode {
	private double size = 0.25;
	private Color fillColor = Color.BLUE.darker();

	private VisualConnection parentConnection;

	public VisualConnection getParentConnection() {
		return parentConnection;
	}

	Shape shape = new Rectangle2D.Double(
			-size / 2,
			-size / 2,
			size,
			size);

	public VisualConnectionAnchorPoint(VisualConnection parent) {
		parentConnection = parent;

		addListener(new PropertyChangeListener() {
			public void onPropertyChanged(String propertyName, Object sender) {
				parentConnection.update();
			}
		});
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		g.setColor(Coloriser.colorise(fillColor, getColorisation()));
		g.fill(shape);
	}

	public Touchable hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
			return this;
		else
			return null;
	}
}
