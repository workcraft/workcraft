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

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.Transition;

@DisplayName("Signal transition")
@VisualClass("org.workcraft.plugins.stg.VisualSignalTransition")
public class SignalTransition extends Transition implements ObservableState {
	public enum Type {
		INPUT,
		OUTPUT,
		INTERNAL,
		DUMMY
	}

	public enum Direction {
		PLUS,
		MINUS,
		TOGGLE
	}

	private Type type = Type.INTERNAL;
	private Direction direction = Direction.TOGGLE;
	private String signalName = "";
	private int instance = 0;

	private ObservableStateImpl observableState = new ObservableStateImpl();

	public Type getSignalType() {
		return type;
	}

	public void setSignalType(Type type) {
		this.type = type;

		observableState.sendNotification(new PropertyChangedEvent(this, "signalType"));
	}

	public String getSignalName() {
		return signalName;
	}

	public void setSignalName(String name) {
		if (name.endsWith("+"))
			setDirection(Direction.PLUS);
		else if (name.endsWith("-"))
			setDirection(Direction.MINUS);
		else if (name.endsWith("~"))
			setDirection(Direction.TOGGLE);

		name = name.replace("+", "");
		name = name.replace("-", "");
		name = name.replace("/", "");
		name = name.replace("~", "");

		signalName = name;

		observableState.sendNotification(new PropertyChangedEvent(this, "signalName"));
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;

		observableState.sendNotification(new PropertyChangedEvent(this, "direction"));
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	public void addObserver(StateObserver obs) {
		observableState.addObserver(obs);
	}

	public void removeObserver(StateObserver obs) {
		observableState.removeObserver(obs);
	}
}
