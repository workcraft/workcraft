package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.PropertyChangeListener;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.Coloriser;


public abstract class VisualConnectionAnchorPoint extends VisualTransformableNode implements Drawable {
	private double size = 0.25;
	private Color fillColor = Color.BLUE.darker();

	protected ConnectionInfo connectionInfo;

	public Node getParentConnection() {
		return connectionInfo.getConnection();
	}

	Shape shape = new Rectangle2D.Double(
			-size / 2,
			-size / 2,
			size,
			size);

	public VisualConnectionAnchorPoint(ConnectionInfo parent) {
		connectionInfo = parent;

		addPropertyChangeListener(new PropertyChangeListener() {
			public void onPropertyChanged(String propertyName, Object sender) {
				connectionInfo.update();
			}
		});
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Coloriser.colorise(fillColor, getColorisation()));
		g.fill(shape);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}
}
