package org.workcraft.plugins.stg;

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;

class SignalTypeConsistencySupervisor extends StateSupervisor {
    private final Stg stg;

    SignalTypeConsistencySupervisor(Stg stg) {
        this.stg = stg;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            String propertyName = pce.getPropertyName();
            if (propertyName.equals(SignalTransition.PROPERTY_SIGNAL_TYPE) ||
                    propertyName.equals(SignalTransition.PROPERTY_SIGNAL_NAME)) {

                SignalTransition t = (SignalTransition) e.getSender();
                String signalName = t.getSignalName();
                Container container = (Container) t.getParent();
                Signal.Type signalType = t.getSignalType();
                final Collection<SignalTransition> transitions = stg.getSignalTransitions(signalName, container);

                if (propertyName.equals(SignalTransition.PROPERTY_SIGNAL_TYPE)) {
                    // If transition type changed than change the type of all other transitions with the same signal name.
                    for (SignalTransition tt : transitions) {
                        tt.setSignalType(signalType);
                    }
                }

                if (propertyName.equals(SignalTransition.PROPERTY_SIGNAL_NAME)) {
                    // If transition signal name changed than change its type to that of existing transitions of the same signal.
                    for (SignalTransition tt : transitions) {
                        if (tt == t) continue;
                        t.setSignalType(tt.getSignalType());
                        break;
                    }
                }
            }
        }
    }

}