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
import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_T)
@DisplayName("Signal Transition")
@SVGIcon("images/icons/svg/signal-transition.svg")
public class VisualSignalTransition extends VisualNamedTransition implements StateObserver {

	public VisualSignalTransition(Transition transition) {
		super(transition);
	}

	@Override
	public String getName() {
		final StringBuffer result = new StringBuffer(getReferencedTransition().getSignalName());
		switch (getReferencedTransition().getDirection()) {
			case PLUS:		result.append("+"); break;
			case MINUS:		result.append("-"); break;
			case TOGGLE: 	result.append("~"); break;
		}
		return result.toString();
	}

	@Override
	public Color getColor() {
		switch (getType()) {
			case INPUT:		return STGSettings.getInputColor();
			case OUTPUT:	return STGSettings.getOutputColor();
			case INTERNAL:	return STGSettings.getInternalColor();
			default:		return STGSettings.getDummyColor();
		}
	}

	@NoAutoSerialisation
	public SignalTransition getReferencedTransition() {
		return (SignalTransition)getReferencedComponent();
	}

	@NoAutoSerialisation
	public void setType(SignalTransition.Type type) {
		getReferencedTransition().setSignalType(type);
		updateRenderedName();
	}

	@NoAutoSerialisation
	public SignalTransition.Type getType() {
		return getReferencedTransition().getSignalType();
	}

	@NoAutoSerialisation
	public void setDirection(SignalTransition.Direction direction) {
		getReferencedTransition().setDirection(direction);
		updateRenderedName();
	}

	@NoAutoSerialisation
	public SignalTransition.Direction getDirection() {
		return getReferencedTransition().getDirection();
	}

	@NoAutoSerialisation
	public void setSignalName(String name) {
		getReferencedTransition().setSignalName(name);
		updateRenderedName();
	}

	@NoAutoSerialisation
	public String getSignalName() {
		return getReferencedTransition().getSignalName();
	}
}
