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
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_D)
@DisplayName("Dummy Transition")
@SVGIcon("images/icons/svg/transition.svg")
public class VisualDummyTransition extends VisualTransition implements StateObserver {
	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Label label = new Label(font, "");

	public VisualDummyTransition(DummyTransition transition) {
		super(transition);

		transition.addObserver(this);

		updateText();
	}

	@Override
	public void draw(DrawRequest r) {
		drawLabelInLocalSpace(r);

		Graphics2D g = r.getGraphics();

		Color background = r.getDecoration().getBackground();
		if(background!=null)
		{
			g.setColor(background);
			g.fill(label.getBoundingBox());
		}

		g.setColor(Coloriser.colorise(getColor(), r.getDecoration().getColorisation()));

		label.draw(g);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return label.getBoundingBox();
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return label.getBoundingBox().contains(pointInLocalSpace);
	}

	private Color getColor() {
		return Color.BLACK;
	}

	@NoAutoSerialisation
	public DummyTransition getReferencedTransition() {
		return (DummyTransition)getReferencedComponent();
	}

	private void updateText()
	{
		transformChanging();
		label.setText(getReferencedTransition().getName());
		transformChanged();
	}

	@NoAutoSerialisation
	public String getName() {
		return getReferencedTransition().getName();
	}

	@NoAutoSerialisation
	public void setName(String name) {
		getReferencedTransition().setName(name);
	}

	@Override
	public void notify(StateEvent e) {
		updateText();
	}
}
