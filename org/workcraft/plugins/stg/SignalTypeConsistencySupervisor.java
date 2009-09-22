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

/**
 *
 */
package org.workcraft.plugins.stg;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.util.Hierarchy;

class SignalTypeConsistencySupervisor extends StateSupervisor {
	private final STG stg;

	SignalTypeConsistencySupervisor(STG stg) {
		this.stg = stg;
	}

	@Override
	public void handleEvent(StateEvent e) {
		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pce = (PropertyChangedEvent)e;
			if (pce.getPropertyName().equals("signalType")) {
				SignalTransition t = (SignalTransition)e.getSender();

				String signalName = t.getSignalName();
				SignalTransition.Type signalType = t.getSignalType();

				if (signalName.isEmpty())
					return;

				for (SignalTransition tt : Hierarchy.getDescendantsOfType(stg.getRoot(), SignalTransition.class)) {
					if (t == tt)
						continue;
					if (signalName.equals(tt.getSignalName()) && !signalType.equals(tt.getSignalType())) {
						tt.setSignalType(signalType);
					}
				}
			} else if (pce.getPropertyName().equals("signalName")) {
				SignalTransition t = (SignalTransition)e.getSender();

				String signalName = t.getSignalName();

				if (signalName.isEmpty())
					return;

				for (SignalTransition tt : Hierarchy.getDescendantsOfType(stg.getRoot(), SignalTransition.class)) {
					if (t == tt)
						continue;
					if (signalName.equals(tt.getSignalName())) {
						t.setSignalType(tt.getSignalType());
						break;
					}
				}
			}
		}
	}
}