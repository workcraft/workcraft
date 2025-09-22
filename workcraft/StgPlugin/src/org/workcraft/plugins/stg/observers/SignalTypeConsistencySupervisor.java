package org.workcraft.plugins.stg.observers;

import org.workcraft.dom.Container;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;

public class SignalTypeConsistencySupervisor extends StateSupervisor {

    private final Stg stg;

    public SignalTypeConsistencySupervisor(Stg stg) {
        this.stg = stg;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if ((e instanceof PropertyChangedEvent pce)
                && (e.getSender() instanceof SignalTransition signalTransition)
                && (signalTransition.getParent() instanceof Container container)
                && SignalTransition.PROPERTY_SIGNAL_TYPE.equals(pce.getPropertyName())) {

            // If transition type changed, then change the type of all other transitions with the same signal name.
            String signalName = signalTransition.getSignalName();
            Signal.Type signalType = signalTransition.getSignalType();
            for (SignalTransition otherSignalTransition : stg.getSignalTransitions(signalName, container)) {
                otherSignalTransition.setSignalTypeQuiet(signalType);
            }
        }
    }

}