package org.workcraft.plugins.xbm.observers;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
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
            if (propertyName.equals(XbmSignal.PROPERTY_TYPE)) {
                final XbmSignal s = (XbmSignal) e.getSender();
                final Collection<XbmState> states = xbm.getXbmStates();
                final Collection<BurstEvent> burstEvents = xbm.getBurstEvents();

                for (XbmState state: states) {
                    if (state.getEncoding().get(s) == null && (s.getType() == XbmSignal.Type.INPUT || s.getType() == XbmSignal.Type.OUTPUT)) {
                        state.addOrChangeSignalValue(s, SignalState.LOW);
                    } else if (s.getType() != XbmSignal.Type.INPUT && s.getType() != XbmSignal.Type.OUTPUT) {
                        state.removeSignal(s);
                    }
                    state.sendNotification(new PropertyChangedEvent(state, XbmState.PROPERTY_ENCODING));
                }
                for (BurstEvent event: burstEvents) {
                    Burst b = event.getBurst();
                    findAndSetTypeForSignal(s, b.getSignals());
                    if (s.getType() != XbmSignal.Type.CONDITIONAL && event.hasConditional()) {
                        if (event.getConditionalMapping().keySet().contains(s.getName())) {
                            event.getConditionalMapping().remove(s.getName());
                        }
                    } else if (s.getType() != XbmSignal.Type.INPUT && s.getType() != XbmSignal.Type.OUTPUT) {
                        event.getBurst().removeSignal(s);
                    }
                }
            }
        }
    }

    private void findAndSetTypeForSignal(XbmSignal s, Collection<XbmSignal> xbmSignals) {
        for (XbmSignal t: xbmSignals) {
            if (t == s) {
                t.setType(s.getType());
                break;
            }
        }
    }
}
