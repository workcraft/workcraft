package org.workcraft.plugins.fst.observers;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.Signal;

public class SignalConsistencySupervisor extends StateSupervisor {

    private final Fst fst;

    public SignalConsistencySupervisor(Fst fst) {
        this.fst = fst;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            if ((sender instanceof Signal) && pce.getPropertyName().equals(Signal.PROPERTY_TYPE)) {
                for (Event event: fst.getEvents((Signal) sender)) {
                    event.sendNotification(new PropertyChangedEvent(event, Signal.PROPERTY_TYPE));
                }
            }
        }
    }
}
