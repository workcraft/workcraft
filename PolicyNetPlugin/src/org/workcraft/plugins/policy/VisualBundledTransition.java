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

package org.workcraft.plugins.policy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.petri.VisualTransition;

@Hotkey(KeyEvent.VK_T)
@DisplayName ("Transition")
@SVGIcon("images/icons/svg/transition.svg")
public class VisualBundledTransition extends VisualTransition {

	public VisualBundledTransition(BundledTransition transition) {
		super(transition);
	}

	public BundledTransition getReferencedTransition() {
		return (BundledTransition)getReferencedComponent();
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		Color background = r.getDecoration().getBackground();
		double xy = -size / 2 + strokeWidth / 2;
		double wh = size - strokeWidth;
		Shape shape = new Rectangle2D.Double (xy, xy, wh, wh);
		g.setColor(Coloriser.colorise(Coloriser.colorise(getFillColor(), background), colorisation));
		g.fill(shape);
		g.setColor(Coloriser.colorise(Coloriser.colorise(getForegroundColor(), background), colorisation));
		g.setStroke(new BasicStroke((float) strokeWidth));
		g.draw(shape);
		drawLabelInLocalSpace(r);
	}

}
