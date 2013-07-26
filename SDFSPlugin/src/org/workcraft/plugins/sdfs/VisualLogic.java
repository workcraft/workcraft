package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;

@Hotkey(KeyEvent.VK_L)
@DisplayName ("Logic")
@SVGIcon("images/icons/svg/sdfs-logic.svg")
public class VisualLogic extends VisualComponent {

	public VisualLogic(Logic logic) {
		super(logic);
	}

	@Override
	public void draw(DrawRequest r) {
		drawLabelInLocalSpace(r);

		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		double size = SDFSVisualSettings.getSize();
		double strokeWidth = SDFSVisualSettings.getStrokeWidth();


		Shape shape = new Rectangle2D.Double (-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(getFillColor(), colorisation));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);
	}

}
