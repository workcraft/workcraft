package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.VisualDtd.SignalEvent;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.traces.Trace;
import org.workcraft.types.Pair;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.ModelUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

public class StgToDtdConverter {

    private static final double SIGNAL_OFFSET = 1.0;
    private static final double EVENT_OFFSET = 1.0;

    private final Stg stg;
    private final VisualDtd dtd;
    private final Map<String, VisualSignal> refToSignalMap;

    public StgToDtdConverter(Stg stg, Trace trace, LinkedList<Pair<String, Color>> signals) {
        this(stg, trace, signals, null);
    }

    public StgToDtdConverter(Stg stg, Trace trace, LinkedList<Pair<String, Color>> signals, VisualDtd dtd) {
        this.stg = stg;
        this.dtd = (dtd == null) ? new VisualDtd(new Dtd()) : dtd;
        refToSignalMap = createSignals(signals);
        setInitialState(trace);
        createEvents(trace);
        removeRedundantArcs();
    }

    private void removeRedundantArcs() {
        for (VisualConnection connection : dtd.getVisualCausalityConnections()) {
            if (ModelUtils.isTransitive(dtd, connection)) {
                dtd.remove(connection);
            }
        }
    }

    public VisualDtd getVisualDtd() {
        return dtd;
    }

    private Signal.Type convertSignalType(org.workcraft.plugins.stg.Signal.Type type) {
        return switch (type) {
            case INPUT -> Signal.Type.INPUT;
            case OUTPUT -> Signal.Type.OUTPUT;
            case INTERNAL -> Signal.Type.INTERNAL;
        };
    }

    private TransitionEvent.Direction getDirection(SignalTransition.Direction direction) {
        return switch (direction) {
            case PLUS -> TransitionEvent.Direction.RISE;
            case MINUS -> TransitionEvent.Direction.FALL;
            default -> null;
        };
    }

    private Map<String, VisualSignal> createSignals(LinkedList<Pair<String, Color>> signals) {
        Map<String, VisualSignal> result = new HashMap<>();
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
        for (String transitionRef : trace) {
            MathNode node = stg.getNodeByReference(transitionRef);
            if (node instanceof SignalTransition transition) {
                String signalRef = stg.getSignalReference(transition);
                if (visitedSignals.contains(signalRef)) continue;
                visitedSignals.add(signalRef);
                if (refToSignalMap.containsKey(signalRef)) {
                    VisualSignal signal = refToSignalMap.get(signalRef);
                    TransitionEvent.Direction direction = getDirection(transition.getDirection());
                    signal.setInitialState(DtdUtils.getPreviousState(direction));
                }
            }
        }
    }

    private void createEvents(Trace trace) {
        Map<MathNode, Set<SignalEvent>> nodeToCauseEventsMap = new HashMap<>();
        double x = 0;
        for (String transitionRef : trace) {
            MathNode node = stg.getNodeByReference(transitionRef);
            boolean processed = false;
            if (node instanceof SignalTransition transition) {
                String signalRef = stg.getSignalReference(transition);
                if (refToSignalMap.containsKey(signalRef)) {
                    VisualSignal signal = refToSignalMap.get(signalRef);
                    TransitionEvent.Direction direction = getDirection(transition.getDirection());
                    SignalEvent curEvent = dtd.appendSignalEvent(signal, direction);
                    x += EVENT_OFFSET;
                    alignSignalExits(x + EVENT_OFFSET);
                    curEvent.edge.setX(x);
                    Set<SignalEvent> allCauseEvents = new HashSet<>();
                    for (MathNode predNode : stg.getPreset(transition)) {
                        Set<SignalEvent> causeEvents = nodeToCauseEventsMap.getOrDefault(predNode, new HashSet<>());
                        allCauseEvents.addAll(causeEvents);
                    }
                    createCausalityArcs(allCauseEvents, curEvent);
                    for (MathNode succNode : stg.getPostset(transition)) {
                        Set<SignalEvent> causeEvents = new HashSet<>();
                        causeEvents.add(curEvent);
                        nodeToCauseEventsMap.put(succNode, causeEvents);
                    }
                    processed = true;
                }
            }
            if (!processed) {
                Set<SignalEvent> propagatedCauseEvents = new HashSet<>();
                for (MathNode predNode : stg.getPreset(node)) {
                    Set<SignalEvent> causeEvents = nodeToCauseEventsMap.getOrDefault(predNode, new HashSet<>());
                    propagatedCauseEvents.addAll(causeEvents);
                }
                for (MathNode predNode : stg.getPreset(node)) {
                    nodeToCauseEventsMap.remove(predNode);
                }
                for (MathNode succNode : stg.getPostset(node)) {
                    nodeToCauseEventsMap.put(succNode, propagatedCauseEvents);
                }
            }
        }
    }

    private void createCausalityArcs(Collection<SignalEvent> predEvents, SignalEvent curEvent) {
        for (SignalEvent predEvent : predEvents) {
            // Prevent adding level arcs (all non-redundant ones are already created)
            if ((predEvent.edge != null) && (curEvent.edge != null)
                    && (predEvent.edge.getParent() != curEvent.edge.getParent())) {

                try {
                    dtd.connect(predEvent.edge, curEvent.edge);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
    }

    private void alignSignalExits(double xExit) {
        for (VisualExitEvent exit: dtd.getVisualSignalExits(null)) {
            if (exit.getX() < xExit) {
                exit.setX(xExit);
            }
        }
    }

}
