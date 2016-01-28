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
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_D)
@DisplayName("Dummy Transition")
@SVGIcon("images/icons/svg/transition.svg")
public class VisualDummyTransition extends VisualNamedTransition implements StateObserver {

	public VisualDummyTransition(DummyTransition dummyTransition) {
		super(dummyTransition);
	}

	@NoAutoSerialisation
	public DummyTransition getReferencedTransition() {
		return (DummyTransition)getReferencedComponent();
	}

	@Override
	public Color getColor() {
		return STGSettings.getDummyColor();
	}

}
