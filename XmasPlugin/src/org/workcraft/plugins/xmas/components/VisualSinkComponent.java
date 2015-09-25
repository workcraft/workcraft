/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.xmas.components;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.plugins.xmas.XmasSettings;

@DisplayName("Sink")
@Hotkey(KeyEvent.VK_O)
@SVGIcon("images/icons/svg/xmas-sink.svg")
public class VisualSinkComponent extends VisualXmasComponent {

	public final double tokenSize = 0.18 * size;

	public VisualSinkComponent(SinkComponent component) {
		super(component);
		if (component.getChildren().isEmpty()) {
			this.addInput("i", Positioning.CENTER);
		}
	}

	public SinkComponent getReferencedSinkComponent() {
		return (SinkComponent)getReferencedComponent();
	}

	@Override
	public Shape getShape() {
		Path2D shape = new Path2D.Double();

		shape.moveTo(0.00, +0.00);
		shape.lineTo(0.00, +0.40 * size);

		shape.moveTo(-0.35 * size, 0.40 * size);
		shape.lineTo(+0.35 * size, 0.40 * size);

		shape.moveTo(-0.20 * size, +0.55 * size);
		shape.lineTo(+0.20 * size, +0.55 * size);

		shape.moveTo(-0.05 * size, +0.70 * size);
		shape.lineTo(+0.05 * size, +0.70 * size);

		return shape;
	}

	public Shape getTokenShape() {
		return new Ellipse2D.Double(+1.4 * tokenSize, +0.6 * tokenSize, tokenSize, tokenSize);
	}

	@Override
	public void draw(DrawRequest r) {
		super.draw(r);
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		if (d instanceof StateDecoration) {
			if (((StateDecoration)d).getState()) {
				g.setStroke(new BasicStroke((float)XmasSettings.getBorderWidth()));
				g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
				Shape shape = transformShape(getTokenShape());
				g.draw(shape);
			}
		}
	}

}
