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

package org.workcraft.plugins.stg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualNamedTransition extends VisualTransition implements StateObserver {
	protected static Color defaultColor = Color.BLACK;
	public static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	protected RenderedText renderedName = new RenderedText(font, "");

	public VisualNamedTransition(Transition transition) {
		super(transition);
		transition.addObserver(this);
		updateRenderedName();
	}

	@Override
	public void draw(DrawRequest r) {
		drawLabelInLocalSpace(r);
		Graphics2D g = r.getGraphics();

		Color background = r.getDecoration().getBackground();
		if(background!=null) {
			g.setColor(background);
			g.fill(renderedName.getBoundingBox());
		}
		g.setColor(Coloriser.colorise(getColor(), r.getDecoration().getColorisation()));
		renderedName.draw(g);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return renderedName.getBoundingBox();
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return renderedName.hitTest(pointInLocalSpace);
	}

	public Color getColor() {
		return defaultColor;
	}

	protected void updateRenderedName() {
		transformChanging();
		renderedName = new RenderedText(font, getName());
		transformChanged();
	}

	public RenderedText getRenderedName() {
		return renderedName;
	}

	public NamedTransition getReferencedTransition() {
		return (NamedTransition)getReferencedComponent();
	}

	@NoAutoSerialisation
	public String getName() {
		return getReferencedTransition().getName();
	}

	@Override
	public void notify(StateEvent e) {
		updateRenderedName();
	}

}
