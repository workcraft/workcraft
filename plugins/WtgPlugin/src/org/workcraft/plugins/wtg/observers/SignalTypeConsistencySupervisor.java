package org.workcraft.plugins.wtg.observers;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.wtg.Wtg;

public class SignalTypeConsistencySupervisor extends StateSupervisor {

    private final Wtg wtg;

    public SignalTypeConsistencySupervisor(Wtg wtg) {
        this.wtg = wtg;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            if (e.getSender() instanceof Signal) {
                String propertyName = pce.getPropertyName();
                if (propertyName.equals(Signal.PROPERTY_NAME)) {
                    updateThisSignalType((Signal) e.getSender());
                }
                if (propertyName.equals(Signal.PROPERTY_TYPE)) {
                    updateOtherSignalType((Signal) e.getSender());
                }
            }
        }
    }

    private void updateThisSignalType(Signal signal) {
        String signalName = wtg.getName(signal);
        if (signalName == null) {
            return;
        }
        for (Signal otherSignal : wtg.getSignals()) {
            if (signal == otherSignal) continue;
            if (signalName.equals(wtg.getName(otherSignal))) {
                signal.setType(otherSignal.getType());
                break;
            }
        }
    }

    private void updateOtherSignalType(Signal signal) {
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