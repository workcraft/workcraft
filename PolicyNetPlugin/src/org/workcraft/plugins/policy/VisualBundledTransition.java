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
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
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
		Decoration d = r.getDecoration();
		VisualPolicyNet model = (VisualPolicyNet)r.getModel();
		double w = size - strokeWidth;
		double h = size - strokeWidth;
		double w2 = w / 2;
		double h2 = h / 2;
		Shape shape = new Rectangle2D.Double (-w2, -h2, w, h);

		Collection<VisualBundle> bundles = model.getBundlesOfTransition(this);
		if (bundles.size() > 0) {
			h = (h - strokeWidth) /bundles.size();
			h2 = h/2;
		}

		if (bundles.isEmpty()) {
			g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
			g.fill(shape);
		} else {
			double y = -size/2 + strokeWidth + h2;
			for (VisualBundle b: bundles) {
				Shape bundleShape = new Rectangle2D.Double (-w2, y-h2, w, h);
				g.setColor(Coloriser.colorise(b.getColor(), d.getBackground()));
				g.fill(bundleShape);
				y += h;
			}
		}
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		g.setStroke(new BasicStroke((float) strokeWidth));
		g.draw(shape);
		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
	}

}
