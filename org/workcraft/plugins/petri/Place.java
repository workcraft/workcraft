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

package org.workcraft.plugins.petri;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateObserver;

@VisualClass("org.workcraft.plugins.petri.VisualPlace")
public class Place extends MathNode implements ObservableState {
	ObservableStateImpl observableStateImpl = new ObservableStateImpl();

	protected int tokens = 0;
	protected int capacity = 1;

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int c) {
		this.capacity = c;
	}

	public int getTokens() {
		return tokens;
	}

	public void setTokens(int tokens) {
		this.tokens = tokens;

		observableStateImpl.sendNotification( new PropertyChangedEvent(this, "tokens") );
	}

	public void addObserver(StateObserver obs) {
		observableStateImpl.addObserver(obs);
	}

	public void removeObserver(StateObserver obs) {
		observableStateImpl.removeObserver(obs);
	}
}