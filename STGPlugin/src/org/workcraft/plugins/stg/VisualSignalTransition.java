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
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/icons/svg/signal-transition.svg")
public class VisualSignalTransition extends VisualTransition implements StateObserver {
	private static Color inputsColor = Color.RED.darker();
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();

	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Label label = new Label(font, "");

	public VisualSignalTransition(Transition transition) {
		super(transition);
		addPropertyDeclarations();

		transition.addObserver(this);

		updateText();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", SignalTransition.Type.INPUT);
		types.put("Output", SignalTransition.Type.OUTPUT);
		types.put("Internal", SignalTransition.Type.INTERNAL);

		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("+", SignalTransition.Direction.PLUS);
		directions.put("-", SignalTransition.Direction.MINUS);
		directions.put("", SignalTransition.Direction.TOGGLE);

		//addPropertyDeclaration(new PropertyDeclaration(this, "Signal name", "getSignalName", "setSignalName", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Transition", "getDirection", "setDirection", SignalTransition.Direction.class, directions));
		addPropertyDeclaration(new PropertyDeclaration(this, "Signal type", "getType", "setType", SignalTransition.Type.class, types));
	}


	@Override
	public void draw(Graphics2D g) {
		drawLabelInLocalSpace(g);

		g.setColor(Coloriser.colorise(getColor(), getColorisation()));

		label.draw(g);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		return label.getBoundingBox();
	}

	private String getText() {
		final StringBuffer result = new StringBuffer(getReferencedTransition().getSignalName());
		switch (getReferencedTransition().getDirection()) {
		case PLUS:
			result.append("+"); break;
		case MINUS:
			result.append("-"); break;
		case TOGGLE:
			result.append("~"); break;
		}
		return result.toString();
	}

	private Color getColor() {
		if (getType() == SignalTransition.Type.INTERNAL)
			return internalsColor;
		if (getType() == SignalTransition.Type.INPUT)
			return inputsColor;
		if (getType() == SignalTransition.Type.OUTPUT)
			return outputsColor;
		return Color.BLACK;
	}

	private void updateText() {
		transformChanging();
		label.setText(getText());
		transformChanged();
	}

	@NoAutoSerialisation
	public SignalTransition getReferencedTransition() {
		return (SignalTransition)getReferencedComponent();
	}

	@NoAutoSerialisation
	public SignalTransition.Type getType() {
		return getReferencedTransition().getSignalType();
	}

	@NoAutoSerialisation
	public void setType(SignalTransition.Type type) {
		getReferencedTransition().setSignalType(type);
		updateText();
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

	@Override
	public void notify(StateEvent e) {
		updateText();
	}
}
