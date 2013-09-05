package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.sdfs.decorations.BinaryRegisterDecoration;

@Hotkey(KeyEvent.VK_D)
@DisplayName ("Control Register")
@SVGIcon("images/icons/svg/sdfs-control_register.svg")
public class VisualControlRegister extends VisualComponent {

	public VisualControlRegister(ControlRegister register) {
		super(register);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> markingChoice = new LinkedHashMap<String, Object>();
		for (ControlRegister.Marking marking : ControlRegister.Marking.values())
			markingChoice.put(marking.name, marking);

		addPropertyDeclaration(new PropertyDeclaration(this, "Marking", "getMarking", "setMarking",
				ControlRegister.Marking.class, markingChoice));
	}

	public ControlRegister getReferencedControlRegister() {
		return (ControlRegister)getReferencedComponent();
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		double w = size - strokeWidth;
		double h = size - strokeWidth;
		double w2 = w/2;
		double w4 = w/4;
		double h2 = h/2;
		double dx = size / 5;
		double dy = strokeWidth / 4;
		float strokeWidth1 = (float)strokeWidth;
		float strokeWidth2 = strokeWidth1 / 2;
		float strokeWidth4 = strokeWidth1 / 4;
		int kd = 6;
		double dd = (size - strokeWidth1 - strokeWidth1) / (4 * kd);
		double tr = (size - strokeWidth1 - strokeWidth1) / 6;

		Path2D shape = new Path2D.Double();
		shape.moveTo(-w2,  0);
		shape.lineTo(-w2 + dx, -h2);
		shape.lineTo(+w2 - dx, -h2);
		shape.lineTo(+w2,   0);
		shape.lineTo(+w2 - dx, +h2);
		shape.lineTo(-w2 + dx, +h2);
		shape.closePath();

		Path2D trueInnerShape = new Path2D.Double();
		trueInnerShape.moveTo(-w2 + dx, -dy);
		trueInnerShape.lineTo(-w2 + dx, -h2);
		trueInnerShape.lineTo( w2 - dx, -h2);
		trueInnerShape.lineTo( w2 - dx, -dy);

		Path2D falseInnerShape = new Path2D.Double();
		falseInnerShape.moveTo( w2 - dx, dy);
		falseInnerShape.lineTo( w2 - dx, h2);
		falseInnerShape.lineTo(-w2 + dx, h2);
		falseInnerShape.lineTo(-w2 + dx, dy);

		Path2D trueShape = new Path2D.Double();
		trueShape.moveTo(-dd, (-kd-2) * dd);
		trueShape.lineTo(+dd, (-kd-2) * dd);
		trueShape.moveTo(  0, (-kd-2) * dd);
		trueShape.lineTo(  0, (-kd+2) * dd);

		Path2D falseShape = new Path2D.Double();
		falseShape.moveTo(+dd, (+kd-2) * dd);
		falseShape.lineTo(-dd, (+kd-2) * dd);
		falseShape.lineTo(-dd, (+kd+2) * dd);
		falseShape.moveTo(+dd, (+kd+0) * dd);
		falseShape.lineTo(-dd, (+kd+0) * dd);

		Shape trueTokenShape = new Ellipse2D.Double( -tr, -w4 - tr + strokeWidth4, 2*tr, 2*tr);
		Shape falseTokenShape = new Ellipse2D.Double(-tr, +w4 - tr - strokeWidth4, 2*tr, 2*tr);
		Shape separatorShape = new Line2D.Double(-w2 + dx, 0, w2 - dx, 0);

		boolean trueMarked = isTrueMarked();
		boolean trueExcited = false;
		boolean falseMarked = isFalseMarked();
		boolean falseExcited = false;
		Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
		if (d instanceof BinaryRegisterDecoration) {
			trueMarked = ((BinaryRegisterDecoration)d).isTrueMarked();
			trueExcited = ((BinaryRegisterDecoration)d).isTrueExcited();
			falseMarked = ((BinaryRegisterDecoration)d).isFalseMarked();
			falseExcited = ((BinaryRegisterDecoration)d).isFalseExcited();
			defaultColor = getForegroundColor();
		}

		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(shape);

		g.setColor(defaultColor);
		if (!trueExcited) {
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(trueInnerShape);
			g.setStroke(new BasicStroke(strokeWidth4));
			g.draw(trueShape);
		}
		if (!falseExcited) {
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(falseInnerShape);
			g.setStroke(new BasicStroke(strokeWidth4));
			g.draw(falseShape);
		}

		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		if (trueExcited) {
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(trueInnerShape);
			g.setStroke(new BasicStroke(strokeWidth4));
			g.draw(trueShape);
		}
		if (falseExcited) {
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(falseInnerShape);
			g.setStroke(new BasicStroke(strokeWidth4));
			g.draw(falseShape);
		}

		if (trueExcited || falseExcited) {
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		} else {
			g.setColor(defaultColor);
		}
		g.setStroke(new BasicStroke(strokeWidth2));
		g.draw(separatorShape);

		g.setStroke(new BasicStroke(strokeWidth1));
		g.draw(shape);

		if (trueMarked) {
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(trueTokenShape);
		}
		if (falseMarked) {
			g.setStroke(new BasicStroke(strokeWidth2));
			g.draw(falseTokenShape);
		}

		drawLabelInLocalSpace(r);
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
	}

	public ControlRegister.Marking getMarking() {
		return getReferencedControlRegister().getMarking();
	}

	public void setMarking(ControlRegister.Marking value) {
		getReferencedControlRegister().setMarking(value);
	}

	public boolean isFalseMarked() {
		return getReferencedControlRegister().isFalseMarked();
	}

	public boolean isTrueMarked() {
		return getReferencedControlRegister().isTrusMarked();
	}

}
