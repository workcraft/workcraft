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
import java.util.LinkedHashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/icons/svg/signal-transition.svg")
public class VisualSignalTransition extends VisualTransition {
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;

	private static Color inputsColor = Color.RED.darker();
	private static Color dummiesColor = Color.BLACK;
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();
	private static Color defaultFillColor = Color.WHITE;
	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Color userFillColor = defaultFillColor;

	private String text = null;
	private GlyphVector glyphVector = null;
	private Rectangle2D textBB = null;
	private Rectangle2D emptyBB = new Rectangle2D.Double(-size/2, -size/2, size, size);
	private float textX, textY;

	public VisualSignalTransition(Transition transition) {
		super(transition);
		addPropertyDeclarations();
		updateText();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", SignalTransition.Type.INPUT);
		types.put("Output", SignalTransition.Type.OUTPUT);
		types.put("Internal", SignalTransition.Type.INTERNAL);
		types.put("Dummy", SignalTransition.Type.DUMMY);

		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("+", SignalTransition.Direction.PLUS);
		directions.put("-", SignalTransition.Direction.MINUS);
		directions.put("", SignalTransition.Direction.TOGGLE);

		addPropertyDeclaration(new PropertyDeclaration("Signal name", "getSignalName", "setSignalName", String.class));
		addPropertyDeclaration(new PropertyDeclaration("Transition", "getDirection", "setDirection", SignalTransition.Direction.class, directions));
		addPropertyDeclaration(new PropertyDeclaration("Signal type", "getType", "setType", SignalTransition.Type.class, types));
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
				firePropertyChanged("shape");
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
		String text = getSignalName();
		if (text.isEmpty())
			return text;

		switch (getDirection()) {
		case PLUS:
			return text+"+";
		case MINUS:
			return text+"-";
		}

		return text;
	}

	private Color getColor() {
		if (getType() == SignalTransition.Type.DUMMY)
			return dummiesColor;
		if (getType() == SignalTransition.Type.INTERNAL)
			return internalsColor;
		if (getType() == SignalTransition.Type.INPUT)
			return inputsColor;
		if (getType() == SignalTransition.Type.OUTPUT)
			return outputsColor;
		return Color.BLACK;
	}

	private void updateText() {
		String signalName = getSignalName();

		if (signalName == null || signalName.isEmpty()) {
			text = null;
		} else {
			text = getText();
		}

		glyphVector = null;
		textBB = null;
	}

	@NoAutoSerialisation
	public SignalTransition getReferencedTransition() {
		return (SignalTransition)getReferencedComponent();
	}

	@NoAutoSerialisation
	public void setReferencedTransition(SignalTransition t) {

	}

	@NoAutoSerialisation
	public SignalTransition.Type getType() {
		return getReferencedTransition().getSignalType();
	}

	@NoAutoSerialisation
	public void setType(SignalTransition.Type type) {
		getReferencedTransition().setSignalType(type);
	}

	@NoAutoSerialisation
	public SignalTransition.Direction getDirection() {
		return getReferencedTransition().getDirection();
	}

	@NoAutoSerialisation
	public void setDirection(SignalTransition.Direction direction) {
		getReferencedTransition().setDirection(direction);
		updateText();
	}

	@NoAutoSerialisation
	public String getSignalName() {
		return getReferencedTransition().getSignalName();
	}

	@NoAutoSerialisation
	public void setSignalName(String name) {
		getReferencedTransition().setSignalName(name);
		updateText();
	}
}
