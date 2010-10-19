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

package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
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

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Transition")
@SVGIcon("images/icons/svg/transition.svg")
public class VisualTransition extends VisualComponent {

	public VisualTransition(Transition transition) {
		super(transition);
	}

	public Transition getReferencedTransition() {
		return (Transition)getReferencedComponent();
	}

	@Override
	public void draw(DrawRequest r) {
		drawLabelInLocalSpace(r);

		Graphics2D g = r.getGraphics();

		double size = CommonVisualSettings.getSize();
		double strokeWidth = CommonVisualSettings.getStrokeWidth();

		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);
		g.setColor(Coloriser.colorise(Coloriser.colorise(getFillColor(), r.getDecoration().getBackground()), r.getDecoration().getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(Coloriser.colorise(getForegroundColor(), r.getDecoration().getBackground()), r.getDecoration().getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		double size = CommonVisualSettings.getSize();
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}
}
