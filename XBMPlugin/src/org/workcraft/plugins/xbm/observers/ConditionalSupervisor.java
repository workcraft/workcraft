package org.workcraft.plugins.xbm.observers;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.xbm.BurstEvent;
import org.workcraft.plugins.xbm.XbmSignal;
import org.workcraft.plugins.xbm.Xbm;

import java.util.Collection;
import java.util.HashSet;

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
                Collection<XbmSignal> declaredExistingCondXbmSignals = new HashSet<>();
                Collection<String> declaredCondSignalsName = event.getConditionalMapping().keySet();

                for (String sigName: declaredCondSignalsName) {
                    Node node = xbm.getNodeByReference(sigName);
                    if (node instanceof XbmSignal) {
                        XbmSignal xbmSignal = (XbmSignal) node;
                        if (xbmSignal.getType() == XbmSignal.Type.CONDITIONAL) {
                            declaredExistingCondXbmSignals.add(xbmSignal);
                        }
                        else {
                            //throw new ArgumentException("The provided literal contains signals that are not specified as a conditional.");
                            throw new ArgumentException("Node " + sigName + " in literal \'" + event.getConditional() +"\' already exists and it is not a conditional xbmSignal.");
                        }
                    }
                    else {
                       throw new ArgumentException("Node " + sigName + " in literal \'" + event.getConditional() +"\' does not exist.");
//                       XbmSignal newConditional = xbm.createNode(sigName, xbm.getRoot(), XbmSignal.class);
//                       newConditional.setName(xbm.getNodeReference(newConditional));
//                       newConditional.setType(XbmSignal.Type.CONDITIONAL);
//                       declaredExistingCondXbmSignals.add(newConditional);
                    }
                }
            }
        }
    }
}