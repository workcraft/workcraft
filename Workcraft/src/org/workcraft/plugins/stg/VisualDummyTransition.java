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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Dummy Transition")
@SVGIcon("images/icons/svg/transition.svg")
public class VisualDummyTransition extends VisualTransition implements StateObserver {
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;

	private static Color defaultFillColor = Color.WHITE;
	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Color userFillColor = defaultFillColor;

	private String text = null;
	private GlyphVector glyphVector = null;
	private Rectangle2D textBB = null;
	private Rectangle2D emptyBB = new Rectangle2D.Double(-size/2, -size/2, size, size);
	private float textX, textY;

	public VisualDummyTransition(Transition transition) {
		super(transition);

		transition.addObserver(this);

		updateText();
	}

	@Override
	public void draw(Graphics2D g) {
		drawLabelInLocalSpace(g);


		// some debug info
		/* int postv = getPostset().size();
		int postm = getReferencedTransition().getPostset().size();
		int prev = getPreset().size();
		int prem = getReferencedTransition().getPreset().size();

		if (postv!=postm||prev!=prem) {

			g.setColor(Color.red);
		    Font font = new Font("Courier", Font.PLAIN, 1);
		    g.setFont(font);

			String str = (postv!=postm)?("POST("+postv+","+postm+")"):"";
			str+=(prev!=prem)?("PRE("+prev+","+prem+")"):"";

			g.drawString("ERROR:"+str, 1, 0);

		}*/


		if (text == null) {
			Shape shape = new Rectangle2D.Double(
					-size / 2 + strokeWidth / 2,
					-size / 2 + strokeWidth / 2,
					size - strokeWidth,
					size - strokeWidth);

			g.setColor(Coloriser.colorise(userFillColor, getColorisation()));
			g.fill(shape);


			g.setColor(Coloriser.colorise(getColor(), getColorisation()));
			g.setStroke(new BasicStroke(strokeWidth));
			g.draw(shape);
		} else {
			g.setColor(Coloriser.colorise(getColor(), getColorisation()));
			g.setFont(font);

			if (textBB == null) {
				glyphVector = font.createGlyphVector(g.getFontRenderContext(), text);
				textBB = glyphVector.getVisualBounds();
				textBB.setRect(textBB.getX() - 0.075, textBB.getY() - 0.075, textBB.getWidth() + 0.15, textBB.getHeight() + 0.15);



				textX = (float)-textBB.getCenterX();
				textY = (float)-textBB.getCenterY();

				textBB.setRect(textBB.getX() - textBB.getCenterX(), textBB.getY() - textBB.getCenterY(), textBB.getWidth(), textBB.getHeight());
			}

			//g.setColor(Coloriser.colorise(userFillColor, getColorisation()));
		//	g.fill(textBB);

			g.setColor(Coloriser.colorise(getColor(), getColorisation()));
			g.drawGlyphVector(glyphVector, textX, textY);
		}
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		if (textBB == null)
			return emptyBB;
		else
			return textBB;
	}

	private String getText() {
		return getReferencedTransition().getName();
	}

	private Color getColor() {
		return Color.BLACK;
	}

	private void updateText() {
		String signalName = getName();

		if (signalName == null || signalName.isEmpty()) {
			text = null;
		} else {
			text = getText();
		}

		glyphVector = null;
		textBB = null;
	}

	@NoAutoSerialisation
	public DummyTransition getReferencedTransition() {
		return (DummyTransition)getReferencedComponent();
	}

	@NoAutoSerialisation
	public String getName() {
		return getReferencedTransition().getName();
	}

	@NoAutoSerialisation
	public void setName(String name) {
		getReferencedTransition().setName(name);
		updateText();
	}

	@Override
	public void notify(StateEvent e) {
		updateText();
	}
}
