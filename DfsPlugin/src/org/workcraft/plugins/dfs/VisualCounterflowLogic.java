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
import org.workcraft.gui.propertyeditor.Getter;
import org.workcraft.gui.propertyeditor.SafePropertyDeclaration;
import org.workcraft.gui.propertyeditor.Setter;
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
		addPropertyDeclaration(new SafePropertyDeclaration<VisualCounterflowLogic, Boolean>(
				this, "Forward Computed",
				new Getter<VisualCounterflowLogic, Boolean>() {
					@Override
					public Boolean eval(VisualCounterflowLogic object) {
						return object.getReferencedCounterflowLogic().isForwardComputed();
					}
				},
				new Setter<VisualCounterflowLogic, Boolean>() {
					@Override
					public void eval(VisualCounterflowLogic object, Boolean value) {
						object.getReferencedCounterflowLogic().setForwardComputed(value);
					}
				},
				Boolean.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualCounterflowLogic, Boolean>(
				this, "Backward Computed",
				new Getter<VisualCounterflowLogic, Boolean>() {
					@Override
					public Boolean eval(VisualCounterflowLogic object) {
						return object.getReferencedCounterflowLogic().isBackwardComputed();
					}
				},
				new Setter<VisualCounterflowLogic, Boolean>() {
					@Override
					public void eval(VisualCounterflowLogic object, Boolean value) {
						object.getReferencedCounterflowLogic().setBackwardComputed(value);
					}
				},
				Boolean.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualCounterflowLogic, Boolean>(
				this, "Forward Early Evaluation",
				new Getter<VisualCounterflowLogic, Boolean>() {
					@Override
					public Boolean eval(VisualCounterflowLogic object) {
						return object.getReferencedCounterflowLogic().isForwardEarlyEvaluation();
					}
				},
				new Setter<VisualCounterflowLogic, Boolean>() {
					@Override
					public void eval(VisualCounterflowLogic object, Boolean value) {
						object.getReferencedCounterflowLogic().setForwardEarlyEvaluation(value);
					}
				},
				Boolean.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualCounterflowLogic, Boolean>(
				this, "Backward Early Evaluation",
				new Getter<VisualCounterflowLogic, Boolean>() {
					@Override
					public Boolean eval(VisualCounterflowLogic object) {
						return object.getReferencedCounterflowLogic().isBackwardEarlyEvaluation();
					}
				},
				new Setter<VisualCounterflowLogic, Boolean>() {
					@Override
					public void eval(VisualCounterflowLogic object, Boolean value) {
						object.getReferencedCounterflowLogic().setBackwardEarlyEvaluation(value);
					}
				},
				Boolean.class));
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
		boolean forwardComputed = getReferencedCounterflowLogic().isForwardComputed();
		boolean forwardComputedExcited = false;
		boolean backwardComputed = getReferencedCounterflowLogic().isBackwardComputed();
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
			if (getReferencedCounterflowLogic().isForwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(forwardEarlyShape);
			}
		}
		if (!backwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(backwardShape);
			if (getReferencedCounterflowLogic().isBackwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(backwardEarlyShape);
			}
		}

		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		if (forwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(forwardShape);
			if (getReferencedCounterflowLogic().isForwardEarlyEvaluation()) {
				g.setStroke(new BasicStroke(strokeWidth4));
				g.draw(forwardEarlyShape);
			}
		}
		if (backwardComputedExcited) {
			g.setStroke(new BasicStroke(strokeWidth1));
			g.draw(backwardShape);
			if (getReferencedCounterflowLogic().isBackwardEarlyEvaluation()) {
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
		drawReferenceInLocalSpace(r);
	}

	public CounterflowLogic getReferencedCounterflowLogic() {
		return (CounterflowLogic)getReferencedComponent();
	}

}
