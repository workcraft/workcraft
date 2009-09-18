package org.workcraft.plugins.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.Hotkey;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;

@Hotkey(KeyEvent.VK_P)
public class VisualVertex extends VisualComponent {
	private static double size = 1;
	private static float strokeWidth = 0.1f;

	public VisualVertex(Vertex vertex) {
		super(vertex);
	}

	public void draw(Graphics2D g) {

		Shape shape = new Ellipse2D.Double(
				-size/2+strokeWidth/2,
				-size/2+strokeWidth/2,
				size-strokeWidth,
				size-strokeWidth);

		g.setStroke(new BasicStroke(strokeWidth));

		g.setColor(Color.WHITE);
		g.fill(shape);
		g.setColor(Color.BLACK);
		g.draw(shape);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0,0) < size * size;
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		// TODO Auto-generated method stub
		return null;
	}
}
