package org.workcraft.plugins.xbm.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.xbm.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
                    handleEncodingChanges((XbmState) node);
                }
                break;

            case Burst.PROPERTY_DIRECTION:
                if (node instanceof BurstEvent) {
                    handleDirectionChanges((BurstEvent) node);
                }
                break;

            case XbmSignal.PROPERTY_NAME:
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
                if (node instanceof XbmSignal) {
                    removeSignalFromNodes((XbmSignal) node);
                }
            }
        } else if (e instanceof NodesAddingEvent) {
            NodesAddingEvent event = (NodesAddingEvent) e;
            for (Node node: event.getAffectedNodes()) {
                if (node instanceof XbmState) {
                    assignSignalsToState((XbmState) node);
                } else if (node instanceof XbmSignal) {
                    XbmSignal xbmSignal = (XbmSignal) node;
                    if (xbmSignal.getType() == XbmSignal.Type.INPUT || xbmSignal.getType() == XbmSignal.Type.OUTPUT) {
                        assignSignalToStates((XbmSignal) node);
                    }
                }
            }
        } else if (e instanceof NodesAddedEvent) {
            NodesAddedEvent event = (NodesAddedEvent) e;
            for (Node node: event.getAffectedNodes()) {
                if (node instanceof XbmState) {
                    XbmState state = (XbmState) node;
                    reassignSignalsInState(state);
                }
            }
        }
    }

    private void assignSignalsToState(XbmState state) {
        for (XbmSignal xbmSignal : xbm.getSignals()) {
            state.addOrChangeSignalValue(xbmSignal, XbmState.DEFAULT_SIGNAL_STATE);
        }
    }

    private void assignSignalToStates(XbmSignal xbmSignal) {
        for (XbmState state: xbm.getXbmStates()) {
            state.addOrChangeSignalValue(xbmSignal, XbmState.DEFAULT_SIGNAL_STATE);
        }
    }

    private void removeSignalFromNodes(XbmSignal xbmSignal) {
        for (XbmState state: xbm.getXbmStates()) {
            state.getEncoding().remove(xbmSignal);
        }
        for (BurstEvent event: xbm.getBurstEvents()) {
            Burst burst = event.getBurst();
            if (event.getConditionalMapping().containsKey(xbmSignal.getName())) {
                event.getConditionalMapping().remove(xbmSignal.getName());
            }
            if (burst.getDirection().containsKey(xbmSignal)) {
                burst.removeSignal(xbmSignal);
            }
        }
    }

    private void reassignSignalsInState(XbmState state) {
        Collection<XbmSignal> xbmSignalsRef = xbm.getSignals();
        Collection<XbmSignal> stateSignalsRef = new HashSet<>(state.getSignals());
        for (XbmSignal xbmSignal: xbmSignalsRef) {
            for (XbmSignal stateXbmSignal : stateSignalsRef) {
                if (!state.getSignals().contains(xbmSignal) && xbmSignal.getName().equals(stateXbmSignal.getName())) {
                    state.addOrChangeSignalValue(xbmSignal, state.getEncoding().get(stateXbmSignal));
                }
            }
        }
    }

    private void handleEncodingChanges(XbmState target) {
        Collection<BurstEvent> burstEvents = xbm.getBurstEvents();
        for (BurstEvent event: burstEvents) {
            Burst b = event.getBurst();
            XbmState from = b.getFrom();
            XbmState to = b.getTo();
            if (from == target || to == target) {
                for (XbmSignal s : from.getSignals()) {
                    b.addOrChangeSignalDirection(s, from.getEncoding().get(s), to.getEncoding().get(s));
                }
            }
        }
    }

    private void handleDirectionChanges(BurstEvent target) {
        Burst burst = target.getBurst();
        XbmState from = burst.getFrom();
        XbmState to = burst.getTo();
        Map<XbmSignal, Direction> direction = burst.getDirection();
        Map<XbmSignal, Direction> dirReader = new HashMap<>(direction);
        for (Map.Entry<XbmSignal, Direction> entry: dirReader.entrySet()) {
            XbmSignal s = entry.getKey();
            Direction d = entry.getValue();
            if (d != null) {
                switch (d) {
                case PLUS:
                    from.addOrChangeSignalValue(s, SignalState.LOW);
                    to.addOrChangeSignalValue(s, SignalState.HIGH);
                    break;
                case MINUS:
                    from.addOrChangeSignalValue(s, SignalState.HIGH);
                    to.addOrChangeSignalValue(s, SignalState.LOW);
                    break;
                case UNSTABLE:
                    to.addOrChangeSignalValue(s, SignalState.DDC);
                    break;
                case STABLE: //This essentially pushes the signal(direction) to the next arc
                    to.addOrChangeSignalValue(s, from.getEncoding().get(s));
                    burst.removeSignal(s);
                    break;
                }
            }
        }
    }
}
