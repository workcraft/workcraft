package org.workcraft.plugins.wtg;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.dtd.Signal;

class SignalTypeConsistencySupervisor extends StateSupervisor {
    private final Wtg wtg;

    SignalTypeConsistencySupervisor(Wtg wtg) {
        this.wtg = wtg;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            String propertyName = pce.getPropertyName();
            if (propertyName.equals(Signal.PROPERTY_TYPE)) {
                updateSignalType((Signal) e.getSender());
            }
        }
    }

    private void updateSignalType(Signal signal) {
        String signalName = wtg.getName(signal);
        if (signalName == null) {
            return;
        }
        for (Signal otherSignal : wtg.getSignals()) {
            if (signal == otherSignal) continue;
            if (signalName.equals(wtg.getName(otherSignal))) {
                otherSignal.setType(signal.getType());
            }
        }
    }

}