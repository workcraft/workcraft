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

import java.util.Collection;
import java.util.HashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.util.Hierarchy;

@VisualClass("org.workcraft.plugins.stg.VisualSTG")
@DisplayName("Signal Transition Graph")
public class STG extends PetriNet {

	public STG() {
		this(null);
	}

	public STG(Container root) {
		super(root);
		new SignalTypeConsistencySupervisor(this).attach(getRoot());
	}

	public Collection<SignalTransition> getSignalTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
	}

	public void assignInstances() {
		HashMap<String, InstanceCounter> instanceCounterMap = new HashMap<String, InstanceCounter>();

		for (SignalTransition t : getSignalTransitions()) {
			String signalName = t.getSignalName();

			InstanceCounter instanceCounter = instanceCounterMap.get(signalName);

			if (instanceCounter == null)
				instanceCounter = new InstanceCounter();

			switch (t.getDirection()) {
			case PLUS:
				t.setInstance(instanceCounter.plusCounter++);
				break;
			case MINUS:
				t.setInstance(instanceCounter.minusCounter++);
				break;
			case TOGGLE:
				t.setInstance(instanceCounter.toggleCounter++);
				break;
			}

			instanceCounterMap.put(signalName, instanceCounter);
		}
	}

	final public SignalTransition createSignalTransition() {
		SignalTransition ret = new SignalTransition();
		getRoot().add(ret);
		return ret;
	}
}

class InstanceCounter {
	public int plusCounter = 1;
	public int minusCounter = 1;
	public int toggleCounter = 1;
}