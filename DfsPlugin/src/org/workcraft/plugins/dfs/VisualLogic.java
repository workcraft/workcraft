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
		addPropertyDeclaration(new PropertyDeclaration (this, "Indicating", "isIndicating", "setIndicating", boolean.class));
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

		Shape shape = new Rectangle2D.Double (-w2, -h2, w, h);
		Path2D eeShape = new Path2D.Double();
		eeShape.moveTo(-w2, -h2 + strokeWidth4);
		eeShape.lineTo( w2 - strokeWidth1, 0);
		eeShape.lineTo(-w2, h2 - strokeWidth4);

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
		if (!isIndicating()) {
			g.setStroke(new BasicStroke(strokeWidth2));
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

	public void setComputed(boolean computed) {
		getReferencedLogic().setComputed(computed);
	}

	public boolean isIndicating() {
		return getReferencedLogic().isIndicating();
	}

	public void setIndicating(boolean indicating) {
		getReferencedLogic().setIndicating(indicating);
	}

}
