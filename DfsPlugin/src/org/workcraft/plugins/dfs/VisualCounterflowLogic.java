package org.workcraft.plugins.dfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.dfs.decorations.CounterflowLogicDecoration;

@Hotkey(KeyEvent.VK_K)
@DisplayName ("Counterflow logic")
@SVGIcon("images/icons/svg/dfs-counterflow_logic.svg")
public class VisualCounterflowLogic extends VisualComponent {

	public VisualCounterflowLogic(CounterflowLogic logic) {
		super(logic);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Forward Computed",
				"isForwardComputed", "setForwardComputed", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Forward Early Evaluation",
				"isForwardEarlyEvaluation", "setForwardEarlyEvaluation", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Backward Computed",
				"isBackwardComputed", "setBackwardComputed", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Backward Early Evaluation",
				"isBackwardEarlyEvaluation", "setBackwardEarlyEvaluation", boolean.class));
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		double w = size - strokeWidth;
		double h = size - strokeWidth;
		double w2 = w/2;
		double h2 = h/2;
		float strokeWidth1 = (float)strokeWidth;
		float strokeWidth2 = strokeWidth1 / 2;
		float strokeWidth4 = strokeWidth1 / 4;
		int kd = 6;
		double dd = (size - strokeWidth1 - strokeWidth1) / (4 * kd);

		Path2D forwardShape = new Path2D.Double();
		forwardShape.moveTo(-w2, -strokeWidth4);
		forwardShape.lineTo(-w2, -h2);
		forwardShape.lineTo( w2, -h2);
		forwardShape.lineTo( w2, -strokeWidth4);

		Path2D forwardEarlyShape = new Path2D.Double();
		forwardEarlyShape.moveTo(-2*dd + dd, (-kd-2) * dd);
		forwardEarlyShape.lineTo(-2*dd - dd, (-kd-2) * dd);
		forwardEarlyShape.lineTo(-2*dd - dd, (-kd+2) * dd);
		forwardEarlyShape.lineTo(-2*dd + dd, (-kd+2) * dd);
		forwardEarlyShape.moveTo(-2*dd + dd, (-kd+0) * dd);
		forwardEarlyShape.lineTo(-2*dd - dd, (-kd+0) * dd);
		forwardEarlyShape.moveTo(+2*dd + dd, (-kd-2) * dd);
		forwardEarlyShape.lineTo(+2*dd - dd, (-kd-2) * dd);
		forwardEarlyShape.lineTo(+2*dd - dd, (-kd+2) * dd);
		forwardEarlyShape.lineTo(+2*dd + dd, (-kd+2) * dd);
		forwardEarlyShape.moveTo(+2*dd + dd, (-kd+0) * dd);
		forwardEarlyShape.lineTo(+2*dd - dd, (-kd+0) * dd);

		Path2D backwardShape = new Path2D.Double();
		backwardShape.moveTo( w2, strokeWidth4);
		backwardShape.lineTo( w2, h2);
		backwardShape.lineTo(-w2, h2);
		backwardShape.lineTo(-w2, strokeWidth4);

		Path2D backwardEarlyShape = new Path2D.Double();
		backwardEarlyShape.moveTo(-2*dd + dd, (+kd-2) * dd);
		backwardEarlyShape.lineTo(-2*dd - dd, (+kd-2) * dd);
		backwardEarlyShape.lineTo(-2*dd - dd, (+kd+2) * dd);
		backwardEarlyShape.lineTo(-2*dd + dd, (+kd+2) * dd);
		backwardEarlyShape.moveTo(-2*dd + dd, (+kd+0) * dd);
		backwardEarlyShape.lineTo(-2*dd - dd, (+kd+0) * dd);
		backwardEarlyShape.moveTo(+2*dd + dd, (+kd-2) * dd);
		backwardEarlyShape.lineTo(+2*dd - dd, (+kd-2) * dd);
		backwardEarlyShape.lineTo(+2*dd - dd, (+kd+2) * dd);
		backwardEarlyShape.lineTo(+2*dd + dd, (+kd+2) * dd);
		backwardEarlyShape.moveTo(+2*dd + dd, (+kd+0) * dd);
		backwardEarlyShape.lineTo(+2*dd - dd, (+kd+0) * dd);

		Shape separatorShape = new Line2D.Double(-w2, 0, w2, 0);

		Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
		boolean forwardComputed = isForwardComputed();
		boolean forwardComputedExcited = false;
		boolean backwardComputed = isBackwardComputed();
		boolean backwardComputedExcited = false;
		if (d instanceof CounterflowLogicDecoration) {
			defaultColor = getForegroundColor();
			forwardComputed = ((CounterflowLogicDecoration)d).isForwardComputed();
			forwardComputedExcited = ((CounterflowLogicDecoration)d).isForwardComputedExcited();
			backwardComputed = ((CounterflowLogicDecoration)d).isBackwardComputed();
			backwardComputedExcited = ((CounterflowLogicDecoration)d).isBackwardComputedExcited();
		}

		if (forwardComputed) {
			g.setColor(Coloriser.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(forwardShape);

		if (backwardComputed) {
			g.setColor(Coloriser.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(backwardShape);

		g.setColor(defaultColor);
		if (!forwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(forwardShape);
			if (isForwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(forwardEarlyShape);
			}
		}
		if (!backwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(backwardShape);
			if (isBackwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(backwardEarlyShape);
			}
		}

		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		if (forwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(forwardShape);
			if (isForwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(forwardEarlyShape);
			}
		}
		if (backwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(backwardShape);
			if (isBackwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(backwardEarlyShape);
			}
		}

		if (forwardComputedExcited || backwardComputedExcited) {
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		} else {
			g.setColor(defaultColor);
		}
		g.setStroke(new BasicStroke(strokeWidth2));
		g.draw(separatorShape);

		drawLabelInLocalSpace(r);
	}

	public CounterflowLogic getReferencedCounterflowLogic() {
		return (CounterflowLogic)getReferencedComponent();
	}

	public boolean isForwardComputed() {
		return getReferencedCounterflowLogic().isForwardComputed();
	}

	public void setForwardComputed(boolean value) {
		getReferencedCounterflowLogic().setForwardComputed(value);
	}

	public boolean isBackwardComputed() {
		return getReferencedCounterflowLogic().isBackwardComputed();
	}

	public void setBackwardComputed(boolean value) {
		getReferencedCounterflowLogic().setBackwardComputed(value);
	}

	public boolean isForwardEarlyEvaluation() {
		return getReferencedCounterflowLogic().isForwardEarlyEvaluation();
	}

	public void setForwardEarlyEvaluation(boolean value) {
		getReferencedCounterflowLogic().setForwardEarlyEvaluation(value);
	}

	public boolean isBackwardEarlyEvaluation() {
		return getReferencedCounterflowLogic().isBackwardEarlyEvaluation();
	}

	public void setBackwardEarlyEvaluation(boolean value) {
		getReferencedCounterflowLogic().setBackwardEarlyEvaluation(value);
	}

}
