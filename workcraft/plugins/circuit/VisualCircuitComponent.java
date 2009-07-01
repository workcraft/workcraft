package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualCircuitComponent extends VisualComponent {

	public VisualCircuitComponent(CircuitComponent component) {
		super(component);
	}

	public VisualCircuitComponent(CircuitComponent component, Element xmlElement) {
		super(component, xmlElement);
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		drawLabelInLocalSpace(g);

		double size = CommonVisualSettings.getSize();
		double strokeWidth = CommonVisualSettings.getStrokeWidth();


		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(getFillColor(), getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}


	@Override
	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
			return 1;
		else
			return 0;
	}

}
