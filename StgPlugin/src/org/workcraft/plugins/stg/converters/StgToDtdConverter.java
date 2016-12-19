package org.workcraft.plugins.stg.converters;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Trace;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.Dtd;
import org.workcraft.plugins.dtd.Signal.Type;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualDtd.SignalEvent;
import org.workcraft.plugins.dtd.VisualSignal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.util.Pair;

public class StgToDtdConverter {
    private static final double SIGNAL_OFFSET = 1.5;
    private static final double EVENT_OFFSET = 1.0;

    private final Stg stg;
    private final VisualDtd dtd;
    private final HashMap<String, VisualSignal> signalMap;
    private final HashMap<SignalEvent, SignalTransition> eventMap;

    public StgToDtdConverter(Stg stg, Trace trace, LinkedList<Pair<String, Color>> signals) {
        this(stg, trace, signals, null);
    }

    public StgToDtdConverter(Stg stg, Trace trace, LinkedList<Pair<String, Color>> signals, VisualDtd dtd) {
        this.stg = stg;
        this.dtd = (dtd == null) ? new VisualDtd(new Dtd()) : dtd;
        signalMap = createSignals(signals);
        eventMap = ctreateEvents(trace);
    }

    public VisualDtd getVisualDtd() {
        return dtd;
    }

    private Type convertSignalType(SignalTransition.Type type) {
        switch (type) {
        case INPUT:    return Type.INPUT;
        case OUTPUT:   return Type.OUTPUT;
        case INTERNAL: return Type.INTERNAL;
        default:       return null;
        }
    }

    private Direction getDirection(SignalTransition.Direction direction) {
        switch (direction) {
        case PLUS:  return Direction.PLUS;
        case MINUS: return Direction.MINUS;
        default:    return null;
        }
    }

    private HashMap<String, VisualSignal> createSignals(LinkedList<Pair<String, Color>> signals) {
        HashMap<String, VisualSignal> result = new HashMap<>();
        for (Pair<String, Color> signalData: signals) {
            String ref = signalData.getFirst();
            Color color = signalData.getSecond();
            String flatName = NamespaceHelper.hierarchicalToFlatName(ref);
            VisualSignal signal = dtd.createVisualSignal(flatName);
            signal.setPosition(new Point2D.Double(0.0, SIGNAL_OFFSET * result.size()));
            Type type = convertSignalType(stg.getSignalType(ref));
            signal.setType(type);
            signal.setForegroundColor(color);
            result.put(ref, signal);
        }
        return result;
    }

    private HashMap<SignalEvent, SignalTransition> ctreateEvents(Trace trace) {
        HashMap<SignalEvent, SignalTransition> result = new HashMap<>();
        HashMap<Node, HashSet<SignalEvent>> causeMap = new HashMap<>();
        double x = 0.0;
        for (String transitionRef: trace) {
            Node node = stg.getNodeByReference(transitionRef);
            boolean skip = true;
            if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalRef = stg.getSignalReference(transition);
                skip = !signalMap.containsKey(signalRef);
            }
            if (skip) {
                HashSet<SignalEvent> propagatedCauses = new HashSet<>();
                for (Node pred: stg.getPreset(node)) {
                    HashSet<SignalEvent> cause = causeMap.get(pred);
                    if (cause != null) {
                        propagatedCauses.addAll(cause);
                        causeMap.remove(pred);
                    }
                }
                for (Node succ: stg.getPostset(node)) {
                    causeMap.put(succ, propagatedCauses);
                }
            } else if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalRef = stg.getSignalReference(transition);
                VisualSignal signal = signalMap.get(signalRef);
                Direction direction = getDirection(transition.getDirection());
                SignalEvent curEvent = dtd.appendSignalEvent(signal, direction);
                result.put(curEvent, transition);
                x += EVENT_OFFSET;
                curEvent.edge.setX(x);
                for (Node pred: stg.getPreset(transition)) {
                    HashSet<SignalEvent> predEvents = causeMap.get(pred);
                    if (predEvents == null) continue;
                    for (SignalEvent predEvent: predEvents) {
                        try {
                            dtd.connect(predEvent.edge, curEvent.edge);
                        } catch (InvalidConnectionException e) {
                        }
                    }
                    causeMap.remove(pred);
                }
                for (Node succ: stg.getPostset(transition)) {
                    HashSet<SignalEvent> signalEvents = new HashSet<>();
                    signalEvents.add(curEvent);
                    causeMap.put(succ, signalEvents);
                }
            }
        }
        return result;
    }

    public VisualSignal getDtdVisualSignal(String signalName) {
        return (signalMap == null) ? null : signalMap.get(signalName);
    }

    public SignalTransition getStgSignalTransition(SignalEvent event) {
        return (eventMap == null) ? null : eventMap.get(event);
    }

}
