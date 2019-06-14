package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.VisualDtd.SignalEvent;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.types.Pair;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class StgToDtdConverter {
    private static final double SIGNAL_OFFSET = 1.0;
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
        setInitialState(trace);
        eventMap = ctreateEvents(trace);
    }

    public VisualDtd getVisualDtd() {
        return dtd;
    }

    private Signal.Type convertSignalType(org.workcraft.plugins.stg.Signal.Type type) {
        switch (type) {
        case INPUT:    return Signal.Type.INPUT;
        case OUTPUT:   return Signal.Type.OUTPUT;
        case INTERNAL: return Signal.Type.INTERNAL;
        default:       return null;
        }
    }

    private TransitionEvent.Direction getDirection(SignalTransition.Direction direction) {
        switch (direction) {
        case PLUS:  return TransitionEvent.Direction.RISE;
        case MINUS: return TransitionEvent.Direction.FALL;
        default:    return null;
        }
    }

    private HashMap<String, VisualSignal> createSignals(LinkedList<Pair<String, Color>> signals) {
        HashMap<String, VisualSignal> result = new HashMap<>();
        for (Pair<String, Color> signalData: signals) {
            String ref = signalData.getFirst();
            Color color = signalData.getSecond();
            String flatName = NamespaceHelper.flattenReference(ref);
            VisualSignal signal = createSignal(flatName);
            signal.setPosition(new Point2D.Double(0.0, SIGNAL_OFFSET * result.size()));
            Signal.Type type = convertSignalType(stg.getSignalType(ref));
            signal.setType(type);
            signal.setForegroundColor(color);
            result.put(ref, signal);
        }
        return result;
    }

    private VisualSignal createSignal(String name) {
        Signal mathSignal = new Signal();
        dtd.getMathModel().add(mathSignal);
        dtd.getMathModel().setName(mathSignal, name);
        VisualSignal visualSignal = new VisualSignal(mathSignal);
        dtd.add(visualSignal);
        dtd.createSignalEntryAndExit(visualSignal);
        return visualSignal;
    }

    private void setInitialState(Trace trace) {
        Set<String> visitedSignals = new HashSet<>();
        for (String transitionRef: trace) {
            MathNode node = stg.getNodeByReference(transitionRef);
            if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalRef = stg.getSignalReference(transition);
                if (visitedSignals.contains(signalRef)) continue;
                visitedSignals.add(signalRef);
                if (signalMap.containsKey(signalRef)) {
                    VisualSignal signal = signalMap.get(signalRef);
                    TransitionEvent.Direction direction = getDirection(transition.getDirection());
                    signal.setInitialState(DtdUtils.getPreviousState(direction));
                }
            }
        }
    }

    private HashMap<SignalEvent, SignalTransition> ctreateEvents(Trace trace) {
        HashMap<SignalEvent, SignalTransition> result = new HashMap<>();
        HashMap<Node, HashSet<SignalEvent>> causeMap = new HashMap<>();
        double x = EVENT_OFFSET;
        for (String transitionRef: trace) {
            MathNode node = stg.getNodeByReference(transitionRef);
            if (node == null) continue;
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
                for (MathNode succ: stg.getPostset(node)) {
                    causeMap.put(succ, propagatedCauses);
                }
            } else if (node instanceof SignalTransition) {
                SignalTransition transition = (SignalTransition) node;
                String signalRef = stg.getSignalReference(transition);
                VisualSignal signal = signalMap.get(signalRef);
                TransitionEvent.Direction direction = getDirection(transition.getDirection());
                SignalEvent curEvent = dtd.appendSignalEvent(signal, direction);
                result.put(curEvent, transition);
                x += EVENT_OFFSET;
                alignSignalExits(x + EVENT_OFFSET);
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

    private void alignSignalExits(double xExit) {
        for (VisualExitEvent exit: dtd.getVisualSignalExits(null)) {
            if (exit.getX() < xExit) {
                exit.setX(xExit);
            }
        }
    }

    public VisualSignal getDtdVisualSignal(String signalName) {
        return (signalMap == null) ? null : signalMap.get(signalName);
    }

    public SignalTransition getStgSignalTransition(SignalEvent event) {
        return (eventMap == null) ? null : eventMap.get(event);
    }

}
