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

package org.workcraft.plugins.sdfs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.sdfs.decorations.RegisterDecoration;

@Hotkey(KeyEvent.VK_R)
@DisplayName ("Register")
@SVGIcon("images/icons/svg/sdfs-register.svg")
public class VisualRegister extends VisualComponent {

	public VisualRegister(Register register) {
		super(register);
		addPropertyDeclarations();
	}

	public Register getReferencedRegister() {
		return (Register)getReferencedComponent();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Marked", "isMarked", "setMarked", boolean.class));
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		double w = size - strokeWidth;
		double h = size - strokeWidth;
		double w2 = w/2;
		double h2 = h/2;
		double dx = size / 5;
		double dy = strokeWidth / 4;
		double dt = (size - strokeWidth) / 8;
		float strokeWidth1 = (float)strokeWidth;
		float strokeWidth2 = strokeWidth1 / 2;

		Shape shape = new Rectangle2D.Double(-w2, -h2, w, h);
		Shape innerShape = new Rectangle2D.Double(-w2 + dx, -h2 + dy, w - dx - dx, h - dy - dy);
		Shape tokenShape = new Ellipse2D.Double(-dt , -dt, 2 * dt, 2 * dt);

		boolean marked = isMarked();
		boolean excited = false;
		Color defaultColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
		if (d instanceof RegisterDecoration) {
			marked = ((RegisterDecoration)d).isMarked();
			excited = ((RegisterDecoration)d).isExcited();
			defaultColor = getForegroundColor();
		}

		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(shape);

		if (excited) {
			g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		} else {
			g.setColor(defaultColor);
		}
		g.setStroke(new BasicStroke(strokeWidth2));
		g.draw(innerShape);
		g.setStroke(new BasicStroke(strokeWidth1));
		g.draw(shape);
		if (marked) {
			g.fill(tokenShape);
		}

		drawLabelInLocalSpace(r);
	}

	public boolean isMarked() {
		return getReferencedRegister().isMarked();
	}

	public void setMarked(boolean marked) {
		getReferencedRegister().setMarked(marked);
	}

}
