package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.framework.plugins.HotKeyDeclaration;
import org.workcraft.gui.Coloriser;

@HotKeyDeclaration(KeyEvent.VK_T)
public class VisualTransition extends VisualComponent {
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;
	private static Color defaultBorderColor = Color.BLACK;
	private static Color defaultFillColor = Color.WHITE;
	private Color userBorderColor = defaultBorderColor;
	private Color userFillColor = defaultFillColor;

	public VisualTransition(Transition transition) {
		super(transition);
	}

	public VisualTransition(Transition transition, Element xmlElement) {
		super(transition, xmlElement);
	}

	public Transition getTransition() {
		return (Transition)getReferencedComponent();
	}

	public boolean isEnabled() {
		return getTransition().isEnabled();
	}

	public void fire() {
		getTransition().fire();
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g) {
		drawLabelInLocalSpace(g);

		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);
		g.setColor(Coloriser.colorise(userFillColor, getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(userBorderColor, getColorisation()));
		g.setStroke(new BasicStroke(strokeWidth));
		g.draw(shape);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}

	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (getBoundingBoxInLocalSpace().contains(pointInLocalSpace))
			return 1;
		else
			return 0;
	}
}
