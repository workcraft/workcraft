package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Transition")
public class VisualTransition extends VisualComponent {

	public VisualTransition(Transition transition) {
		super(transition);
	}

	public Transition getTransition() {
		return (Transition)getReferencedComponent();
	}

	@Override
	public void draw(Graphics2D g) {
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

	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		LinkedList<MathNode> result = new LinkedList<MathNode>();
		result.add(getReferencedComponent());
		return result;
	}
}
