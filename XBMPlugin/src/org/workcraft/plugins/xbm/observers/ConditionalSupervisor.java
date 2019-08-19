package org.workcraft.plugins.xbm.observers;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.xbm.Burst;
import org.workcraft.plugins.xbm.BurstEvent;
import org.workcraft.plugins.xbm.Signal;
import org.workcraft.plugins.xbm.Xbm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConditionalSupervisor extends StateSupervisor {

    private final Xbm xbm;

    public ConditionalSupervisor(Xbm xbm) {
        this.xbm = xbm;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            String propertyName = pce.getPropertyName();
            if (propertyName.equals(BurstEvent.PROPERTY_CONDITIONAL)) {

                BurstEvent event = (BurstEvent) e.getSender();
                Collection<Signal> declaredExistingCondSignals = new HashSet<>();
                Collection<String> declaredCondSignalsName = event.getConditionalMapping().keySet();

                for (String sigName : declaredCondSignalsName) {
                    Node node = xbm.getNodeByReference(sigName);
                    if (node instanceof Signal) {
                        Signal signal = (Signal) node;
                        if (signal.getType() == Signal.Type.CONDITIONAL) {
                            declaredExistingCondSignals.add(signal);
                        }
                        else {
                            throw new ArgumentException("The provided literal contains signals that are not specified as a conditional.");
                        }
                    } else {
                        throw new ArgumentException("The provided literal contains non-existent signals.");
                    }
                }
            }
        }
    }
}