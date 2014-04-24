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

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

public class VisualNamedTransition extends VisualTransition implements StateObserver {
	public static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);
	private RenderedText renderedText = new RenderedText("", font, Positioning.CENTER, getRenderedTextOffset());

	public VisualNamedTransition(Transition transition) {
		super(transition, false, false, false);
		transition.addObserver(this);
		updateRenderedText();
	}

	public Point2D getRenderedTextOffset() {
		return new Point2D.Double(0.0, 0.0);
	}

	@Override
	public boolean getLabelVisibility() {
		return false;
	}

	@Override
	public Point2D getLabelOffset() {
		return new Point2D.Double(0.0,0.0);
	}

	@Override
	public boolean getNameVisibility() {
		return false;
	}

	@Override
	public Point2D getNameOffset() {
		return new Point2D.Double(0.0,0.0);
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		Color background = d.getBackground();
		if (background != null) {
			Rectangle2D shape = getBoundingBoxInLocalSpace();
			g.setColor(background);
			g.fill(shape);
		}
		g.setColor(Coloriser.colorise(getColor(), d.getColorisation()));
		renderedText.draw(g);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return BoundingBoxHelper.expand(renderedText.getBoundingBox(), 0.2, 0.2);
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	public Color getColor() {
		return Color.BLACK;
	}

	protected void updateRenderedText() {
		Point2D offset = getRenderedTextOffset();
		if (renderedText.isDifferent(getName(), font, Positioning.CENTER, offset)) {
			transformChanging(true);
			renderedText = new RenderedText(getName(), font, Positioning.CENTER, offset);
			transformChanged(true);
		}
	}

	public RenderedText getRenderedName() {
		return renderedText;
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
		updateRenderedText();
	}

}
