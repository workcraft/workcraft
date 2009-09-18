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