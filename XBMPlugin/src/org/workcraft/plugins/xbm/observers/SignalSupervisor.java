package org.workcraft.plugins.xbm.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.xbm.*;;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.workcraft.plugins.xbm.SignalState.LOW;

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
            Node node = pce.getSender();
            String propertyName = pce.getPropertyName();

            switch (propertyName) {

                case XbmState.PROPERTY_ENCODING:
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
                    break;

                case Burst.PROPERTY_DIRECTION:
                    if (node instanceof BurstEvent) {
                        BurstEvent burstEvent = (BurstEvent) node;
                        Burst burst = burstEvent.getBurst();
                        XbmState from = burst.getFrom();
                        XbmState to = burst.getTo();
                        Map<Signal, Burst.Direction> direction = burstEvent.getBurst().getDirection();
                        for (Map.Entry<Signal, Burst.Direction> entry: direction.entrySet()) {
                            Signal s = entry.getKey();
                            Burst.Direction d = entry.getValue();
                            if (d != null) {
                                switch (d) {
                                    case PLUS:
                                        if (from.getEncoding().get(s) != SignalState.LOW) from.addOrChangeSignalValue(s, SignalState.LOW);
                                        if (to.getEncoding().get(s) != SignalState.HIGH) to.addOrChangeSignalValue(s, SignalState.HIGH);
                                        break;
                                    case MINUS:
                                        if (from.getEncoding().get(s) != SignalState.HIGH) from.addOrChangeSignalValue(s, SignalState.HIGH);
                                        if (to.getEncoding().get(s) != SignalState.LOW) to.addOrChangeSignalValue(s, SignalState.LOW);
                                        break;
                                    case UNSTABLE:
                                        if (to.getEncoding().get(s) != SignalState.DDC) to.addOrChangeSignalValue(s, SignalState.DDC);
                                        break;
                                    case CLEAR: //This essentially pushes the signal(direction) to the next arc
                                        if (to.getEncoding().get(s) != from.getEncoding().get(s)) to.addOrChangeSignalValue(s, from.getEncoding().get(s));
                                        break;
                                }
                            }
                        }
                    }
                    break;

                case Signal.PROPERTY_NAME:
                    Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
                    for (BurstEvent event: burstEvents) {
                        event.setConditional(event.getConditional()); //A rather dirty way of refreshing the burst event
                    }
                    break;
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
                    Signal signal = (Signal) node;
                    if (signal.getType() == Signal.Type.INPUT || signal.getType() == Signal.Type.OUTPUT) {
                        assignSignalToStates((Signal) node);
                    }
                }
            }
        }
        else if (e instanceof NodesAddedEvent) {
            NodesAddedEvent event = (NodesAddedEvent) e;
            for (Node node: event.getAffectedNodes()) {
                if (node instanceof XbmState) {
                    XbmState state = (XbmState) node;
                    reassignSignalsInState(state);

                    String temp = "";
                    for (Map.Entry<Signal, SignalState> entry: state.getEncoding().entrySet()) {
                        if (!temp.isEmpty()) temp += ", ";
                        temp += xbm.getName(entry.getKey()) + "=" + entry.getValue();
                    }
                    System.out.println(xbm.getName(state) + ":" + temp);
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
