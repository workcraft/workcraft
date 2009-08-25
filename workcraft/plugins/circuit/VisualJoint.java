package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.gui.Coloriser;

public class VisualJoint extends VisualCircuitComponent {
	static double jointSize = 0.25;

	public VisualJoint(Joint joint) {
		super(joint);
	}

	@Override
	public void draw(Graphics2D g) {
		drawLabelInLocalSpace(g);


		Shape shape = new Ellipse2D.Double(
				-jointSize / 2,
				-jointSize / 2,
				jointSize,
				jointSize);

		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.fill(shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-jointSize/2, -jointSize/2, jointSize, jointSize);
	}


	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0, 0) < jointSize*jointSize/4;
	}

}
