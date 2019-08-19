package org.workcraft.plugins.xbm.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.xbm.*;

import java.util.Collection;
import java.util.HashSet;

//FIXME Fixed state generation after running the simulation tool
//FIXME However, the copy-paste is still broken
public class SignalSupervisor extends StateSupervisor {

    private final Xbm xbm;

    public SignalSupervisor(Xbm xbm) {
        this.xbm = xbm;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            String propertyName = pce.getPropertyName();
            if (propertyName.equals(XbmState.PROPERTY_ENCODING)) {

                Node node = ((PropertyChangedEvent) e).getSender();
                if (node instanceof XbmState) {
                    XbmState state = (XbmState) node;
                    Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
                    for (BurstEvent event: burstEvents) {
                        Burst b = event.getBurst();
                        XbmState from = b.getFrom();
                        XbmState to = b.getTo();
                        if (from == state || to == state) {
                            for (Signal s: from.getSignals()) {
                                b.addOrChangeSignalDirection(s, from.getEncoding().get(s), to.getEncoding().get(s));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            NodesDeletingEvent event = (NodesDeletingEvent) e;
            for (Node node: event.getAffectedNodes()) {
                if (node instanceof Signal) {
                    removeSignalFromNodes((Signal) node);
                }
            }
        }
        else if (e instanceof NodesAddingEvent) {
            NodesAddingEvent event = (NodesAddingEvent) e;
            for (Node node: event.getAffectedNodes()) {
                if (node instanceof XbmState) {
                    assignSignalsToState((XbmState) node);
                }
                else if (node instanceof Signal) {
                    assignSignalToStates((Signal) node);
                }
            }
        }
        else if (e instanceof NodesAddedEvent) {
            NodesAddedEvent event = (NodesAddedEvent) e;
            for (Node node: event.getAffectedNodes()) {
                if (node instanceof XbmState) {
                    reassignSignalsInState((XbmState) node);
                }
            }
        }
    }

    private void assignSignalsToState(XbmState state) {
        for (Signal signal: xbm.getSignals()) {
            state.addOrChangeSignalValue(signal, XbmState.DEFAULT_SIGNAL_STATE);
        }
    }

    private void assignSignalToStates(Signal signal) {
        for (XbmState state: xbm.getXbmStates()) {
            state.addOrChangeSignalValue(signal, XbmState.DEFAULT_SIGNAL_STATE);
        }
    }

    private void removeSignalFromNodes(Signal signal) {
        for (XbmState state: xbm.getXbmStates()) {
            state.getEncoding().remove(signal);
        }
        for (BurstEvent event: xbm.getBurstEvents()) {
            Burst burst = event.getBurst();
            if (event.getConditionalMapping().containsKey(signal.getName())) {
                event.getConditionalMapping().remove(signal.getName());
            }
            if (burst.getDirection().containsKey(signal)) {
                burst.removeSignal(signal);
            }
        }
    }

    private void reassignSignalsInState(XbmState state) {
        Collection<Signal> xbmSignalsRef = xbm.getSignals();
        Collection<Signal> stateSignalsRef = new HashSet<>();
        stateSignalsRef.addAll(state.getSignals());
        for (Signal xbmSignal: xbmSignalsRef) {
            for (Signal stateSignal: stateSignalsRef) {
                if (!state.getSignals().contains(xbmSignal) && xbmSignal.getName().equals(stateSignal.getName())) {
                    state.addOrChangeSignalValue(xbmSignal, state.getEncoding().get(stateSignal));
                }
            }
        }
    }
}
