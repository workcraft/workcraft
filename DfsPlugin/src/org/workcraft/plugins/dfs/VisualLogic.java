package org.workcraft.plugins.dfs;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.dfs.decorations.LogicDecoration;

@Hotkey(KeyEvent.VK_L)
@DisplayName ("Logic")
@SVGIcon("images/icons/svg/dfs-logic.svg")
public class VisualLogic extends VisualComponent {

	public VisualLogic(Logic logic) {
		super(logic);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Computed",
				"isComputed", "setComputed", boolean.class));
		addPropertyDeclaration(new PropertyDeclaration (this, "Early Evaluation",
				"isEarlyEvaluation", "setEarlyEvaluation", boolean.class));
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
		float strokeWidth4 = strokeWidth1 / 4;
		int kd = 6;
		double dd = (size - strokeWidth1 - strokeWidth1) / (4 * kd);

		Shape shape = new Rectangle2D.Double (-w2, -h2, w, h);
		Path2D eeShape = new Path2D.Double();
		eeShape.moveTo(-2*dd + dd, -2 * dd);
		eeShape.lineTo(-2*dd - dd, -2 * dd);
		eeShape.lineTo(-2*dd - dd, +2 * dd);
		eeShape.lineTo(-2*dd + dd, +2 * dd);
		eeShape.moveTo(-2*dd + dd, 0);
		eeShape.lineTo(-2*dd - dd, 0);
		eeShape.moveTo(+2*dd + dd, -2 * dd);
		eeShape.lineTo(+2*dd - dd, -2 * dd);
		eeShape.lineTo(+2*dd - dd, +2 * dd);
		eeShape.lineTo(+2*dd + dd, +2 * dd);
		eeShape.moveTo(+2*dd + dd, 0);
		eeShape.lineTo(+2*dd - dd, 0);

		boolean computed = isComputed();
		if (d instanceof LogicDecoration) {
			computed = ((LogicDecoration)d).isComputed();
		}
		if (computed) {
			g.setColor(Coloriser.colorise(DfsSettings.getComputedLogicColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		if (isEarlyEvaluation()) {
			g.setStroke(new BasicStroke(strokeWidth4));
			g.draw(eeShape);
		}
		g.setStroke(new BasicStroke(strokeWidth1));
		g.draw(shape);

		drawLabelInLocalSpace(r);
	}

	public Logic getReferencedLogic() {
		return (Logic)getReferencedComponent();
	}

	public boolean isComputed() {
		return getReferencedLogic().isComputed();
	}

	public void setComputed(boolean value) {
		getReferencedLogic().setComputed(value);
	}

	public boolean isEarlyEvaluation() {
		return getReferencedLogic().isEarlyEvaluation();
	}

	public void setEarlyEvaluation(boolean value) {
		getReferencedLogic().setEarlyEvaluation(value);
	}

}
