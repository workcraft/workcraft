package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.plugins.sdfs.tools.LogicDecoration;

@Hotkey(KeyEvent.VK_L)
@DisplayName ("Logic")
@SVGIcon("images/icons/svg/sdfs-logic.svg")
public class VisualLogic extends VisualComponent {

	public VisualLogic(Logic logic) {
		super(logic);
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		double xy = -size / 2 + strokeWidth / 2;
		double wh = size - strokeWidth;
		Shape shape = new Rectangle2D.Double (xy, xy, wh, wh);

		boolean computed = isComputed();
		if (d instanceof LogicDecoration) {
			computed = ((LogicDecoration)d).isComputed();
		}
		if (computed) {
			g.setColor(Coloriser.colorise(SDFSVisualSettings.getComputedLogicColor(), d.getBackground()));
		} else {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		}
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		g.setStroke(new BasicStroke((float) strokeWidth));
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
}
