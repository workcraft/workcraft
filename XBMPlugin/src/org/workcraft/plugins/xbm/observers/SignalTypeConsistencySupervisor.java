package org.workcraft.plugins.xbm.observers;

import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.fsm.State;
import org.workcraft.plugins.xbm.*;

import java.util.Collection;

public class SignalTypeConsistencySupervisor extends StateSupervisor {

    private final Xbm xbm;

    public SignalTypeConsistencySupervisor(Xbm xbm) {
        this.xbm = xbm;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            final PropertyChangedEvent pce = (PropertyChangedEvent) e;
            final String propertyName = pce.getPropertyName();
            if (propertyName.equals(Signal.PROPERTY_TYPE)) {
                final Signal s = (Signal) e.getSender();
                final Collection<State> states = xbm.getStates();
                final Collection<BurstEvent> burstEvents = xbm.getBurstEvents();

                for (State st: states) {
                    if (st instanceof XbmState) {
                        XbmState state = (XbmState) st;
                        findAndSetTypeForSignal(s, state.getSignals());
                        if (s.getType() == Signal.Type.DUMMY || s.getType() == Signal.Type.CONDITIONAL) {
                            state.addOrChangeSignalValue(s, SignalState.LOW);
                        }
                    }
                }
                for (BurstEvent event: burstEvents) {
                    Burst b = event.getBurst();
                    findAndSetTypeForSignal(s, b.getSignals());
                }
            }
        }
    }

    private void findAndSetTypeForSignal(Signal s, Collection<Signal> signals) {
        for (Signal t: signals) {
            if (t == s) {
                t.setType(s.getType());
                break;
            }
        }
    }
}
