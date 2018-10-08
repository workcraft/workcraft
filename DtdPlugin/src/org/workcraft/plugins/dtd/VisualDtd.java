package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.dtd.supervisors.DtdStateSupervisor;
import org.workcraft.plugins.dtd.tools.DtdConnectionTool;
import org.workcraft.plugins.dtd.tools.DtdSelectionTool;
import org.workcraft.plugins.dtd.tools.DtdSignalGeneratorTool;
import org.workcraft.plugins.dtd.utils.DtdUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Queue;

@DisplayName("Digital Timing Diagram")
public class VisualDtd extends AbstractVisualModel {

    public class SignalEvent {
        public final VisualConnection beforeLevel;
        public final VisualTransitionEvent edge;
        public final VisualConnection afterLevel;
        SignalEvent(VisualConnection beforeLevel, VisualTransitionEvent edge, VisualConnection afterLevel) {
            this.beforeLevel = beforeLevel;
            this.edge = edge;
            this.afterLevel = afterLevel;
        }

        public boolean isValid() {
            return (beforeLevel != null) && (edge != null) && (afterLevel != null);
        }
    }

    public VisualDtd(Dtd model) {
        this(model, null);
    }

    public VisualDtd(Dtd model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
        new DtdStateSupervisor(this).attach(getRoot());
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new DtdSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new DtdConnectionTool());
        tools.add(new DtdSignalGeneratorTool());
        setGraphEditorTools(tools);
    }

    private boolean isTransitionReachableFromTransition(VisualTransitionEvent fromTransition, VisualTransitionEvent toTransition) {
        Set<VisualTransitionEvent> visited = new HashSet<>();
        Queue<VisualTransitionEvent> toVisit = new LinkedList<>();
        visited.add(fromTransition);
        toVisit.add(fromTransition);
        while (!toVisit.isEmpty()) {
            VisualTransitionEvent transitionEvent = toVisit.poll();
            for (Node node : getPostset(transitionEvent)) {
                if (node instanceof VisualTransitionEvent) {
                    VisualTransitionEvent successorEvent = (VisualTransitionEvent) node;
                    if (successorEvent == toTransition) {
                        return true;
                    }
                    if (!visited.contains(successorEvent)) {
                        visited.add(successorEvent);
                        toVisit.add(successorEvent);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }

        if (getConnection(first, second) != null) {
            throw new InvalidConnectionException("Connection already exists.");
        }

        if ((first instanceof VisualTransitionEvent) && (second instanceof VisualTransitionEvent)) {
            VisualTransitionEvent firstTransition = (VisualTransitionEvent) first;
            VisualTransitionEvent secondTransition = (VisualTransitionEvent) second;
            if (isTransitionReachableFromTransition(secondTransition, firstTransition)) {
                throw new InvalidConnectionException("Cannot connect transitions in a loop.");
            }
            if (firstTransition.getParent() == secondTransition.getParent()) {
                if (firstTransition.getDirection() == TransitionEvent.Direction.STABILISE) {
                    throw new InvalidConnectionException("Signal at unknown state cannot change.");
                }
                if ((firstTransition.getDirection() != TransitionEvent.Direction.DESTABILISE)
                        && (secondTransition.getDirection() == TransitionEvent.Direction.STABILISE)) {
                    throw new InvalidConnectionException("Only unstable signal can stabilise.");
                }
                if (firstTransition.getDirection() == secondTransition.getDirection()) {
                    throw new InvalidConnectionException("Cannot connect transitions of the same signal and direction.");
                }
            }
            return;
        }

        if ((first instanceof VisualEntryEvent) && (second instanceof VisualExitEvent)) {
            VisualEntryEvent firstEntry = (VisualEntryEvent) first;
            VisualSignal firstSignal = firstEntry.getVisualSignal();
            VisualExitEvent secondExit = (VisualExitEvent) second;
            VisualSignal secondSignal = secondExit.getVisualSignal();
            if (firstSignal != secondSignal) {
                throw new InvalidConnectionException("Cannot relate entry and exit of different signals.");
            }
            return;
        }

        if ((first instanceof VisualEntryEvent) && (second instanceof VisualTransitionEvent)) {
            VisualEntryEvent firstEntry = (VisualEntryEvent) first;
            VisualSignal firstSignal = firstEntry.getVisualSignal();
            VisualTransitionEvent secondTransition = (VisualTransitionEvent) second;
            VisualSignal secondSignal = secondTransition.getVisualSignal();
            if (firstSignal != secondSignal) {
                throw new InvalidConnectionException("Cannot relate entry and transition of different signals.");
            }
            if (firstSignal.getInitialState() == Signal.State.STABLE) {
                throw new InvalidConnectionException("Signal at unknown state cannot change.");
            }
            if ((firstSignal.getInitialState() != Signal.State.UNSTABLE)
                    && (secondTransition.getDirection() == TransitionEvent.Direction.STABILISE)) {
                throw new InvalidConnectionException("Only unstable signal can stabilise.");
            }
            if ((firstSignal.getInitialState() == Signal.State.HIGH)
                    && (secondTransition.getDirection() == TransitionEvent.Direction.RISE)) {
                throw new InvalidConnectionException("Signal is already high.");
            }
            if ((firstSignal.getInitialState() == Signal.State.LOW)
                    && (secondTransition.getDirection() == TransitionEvent.Direction.FALL)) {
                throw new InvalidConnectionException("Signal is already low.");
            }
            return;
        }

        if ((first instanceof VisualTransitionEvent) && (second instanceof VisualExitEvent)) {
            VisualTransitionEvent firstTransition = (VisualTransitionEvent) first;
            VisualSignal firstSignal = firstTransition.getVisualSignal();
            VisualExitEvent secondExit = (VisualExitEvent) second;
            VisualSignal secondSignal = secondExit.getVisualSignal();
            if (firstSignal != secondSignal) {
                throw new InvalidConnectionException("Cannot relate transition and exit of different signals.");
            }
            return;
        }
        throw new InvalidConnectionException("Invalid connection.");
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        VisualComponent v1 = (VisualComponent) first;
        VisualComponent v2 = (VisualComponent) second;
        Node m1 = v1.getReferencedComponent();
        Node m2 = v2.getReferencedComponent();

        if ((v1 instanceof VisualTransitionEvent) && (v2 instanceof VisualTransitionEvent)) {
            if (v1.getX() > v2.getX() - DtdSettings.getTransitionSeparation()) {
                shiftEvents((VisualEvent) v2, v1.getX() - v2.getX() + DtdSettings.getTransitionSeparation());
            }
        }

        if (mConnection == null) {
            mConnection = ((Dtd) getMathModel()).connect(m1, m2);
        }

        Container container = Hierarchy.getNearestContainer(v1, v2);
        VisualConnection vConnection;
        boolean isLevelConnection = DtdUtils.isLevelConnection(mConnection);
        if (isLevelConnection) {
            vConnection = new VisualLevelConnection(mConnection, v1, v2);
        } else {
            vConnection = new VisualConnection(mConnection, v1, v2);
        }
        container.add(vConnection);
        if (isLevelConnection) {
            DtdUtils.decorateVisualLevelConnection(vConnection);
        } else {
            DtdUtils.decorateVisualEventConnection(vConnection);
        }
        return vConnection;
    }

    private void shiftEvents(VisualEvent event, double shiftOffset) {
        //If a node A is connected to a node B, the X position of A cannot be >= to that of B
        //To go around that restriction, we first compute the dependencies between nodes
        //(i.e. the X of B is bigger than the X of A, so B depends on A)
        Map<VisualEvent, Integer> nodeDependencies = new HashMap<>();
        Queue<VisualEvent> toVisit = new LinkedList<>();
        toVisit.add(event);
        while (!toVisit.isEmpty()) {
            VisualEvent visitingEvent = toVisit.poll();
            if (!(visitingEvent instanceof VisualExitEvent)) {
                for (Node node : getPostset(visitingEvent))  {
                    if (node instanceof VisualEvent) {
                        VisualEvent nextEvent = (VisualEvent) node;
                        if (nodeDependencies.containsKey(nextEvent)) {
                            nodeDependencies.computeIfPresent(nextEvent, (k, v) -> v + 1);
                        } else {
                            nodeDependencies.put(nextEvent, 1);
                            toVisit.add(nextEvent);
                        }
                    }
                }
            }
        }

        //Now we traverse the dependency tree and compute the new X that every node will be set to
        Map<VisualEvent, Double> nodesX = new HashMap<>();
        nodesX.put(event, event.getX() + shiftOffset);
        toVisit.add(event);
        while (!toVisit.isEmpty()) {
            VisualEvent visitingEvent = toVisit.poll();
            for (Node node : getPostset(visitingEvent))  {
                VisualEvent nextEvent = (VisualEvent) node;
                if (nodeDependencies.containsKey(nextEvent)) {
                    double newX;
                    if (nextEvent.getX() - nodesX.get(visitingEvent) > DtdSettings.getTransitionSeparation()) {
                        //Distance to next is large enough that it is not necessary to update it
                        newX = nextEvent.getX();
                    } else if (nextEvent.getX() - visitingEvent.getX() < DtdSettings.getTransitionSeparation()) {
                        //Original distance between transitions was smaller than separation, we keep it that way
                        newX = nodesX.get(visitingEvent) + nextEvent.getX() - visitingEvent.getX();
                    } else {
                        //Original distance was larger than separation, so we default to separation distance
                        newX = nodesX.get(visitingEvent) + DtdSettings.getTransitionSeparation();
                    }
                    nodesX.computeIfPresent(nextEvent, (k, v) -> Math.max(v, newX));
                    nodesX.putIfAbsent(nextEvent, Math.max(nextEvent.getX(), newX));

                    Integer dependencies = nodeDependencies.computeIfPresent(nextEvent, (k, v) -> v - 1);
                    if (dependencies == 0) {
                        toVisit.add(nextEvent);
                    }
                }
            }
        }

        //Finally, we have to set the new Xs starting from right to left (larger to smaller)
        ArrayList<Pair<VisualEvent, Double>> visualEvents = new ArrayList<>();
        for (Map.Entry<VisualEvent, Double> eventsNewX : nodesX.entrySet()) {
            visualEvents.add(new Pair<>(eventsNewX.getKey(), eventsNewX.getValue()));
        }
        visualEvents.sort((p1, p2) -> (p1.getSecond().compareTo(p2.getSecond())) * (-1));
        for (Pair<VisualEvent, Double> visualEventPosition : visualEvents) {
            visualEventPosition.getFirst().setX(visualEventPosition.getSecond());
        }
    }

    public Collection<VisualSignal> getVisualSignals(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getChildrenOfType(container, VisualSignal.class);
    }

    public Collection<VisualTransitionEvent> getVisualSignalTransitions(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, VisualTransitionEvent.class);
    }

    public Collection<VisualEntryEvent> getVisualSignalEntries(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, VisualEntryEvent.class);
    }

    public Collection<VisualExitEvent> getVisualSignalExits(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, VisualExitEvent.class);
    }

    public VisualSignal createVisualSignal(String name) {
        Signal mathSignal = new Signal();
        getMathModel().add(mathSignal);
        getMathModel().setName(mathSignal, name);
        VisualSignal visualSignal = new VisualSignal(mathSignal);
        add(visualSignal);
        createSignalEntryAndExit(visualSignal);
        return visualSignal;
    }

    public void createSignalEntryAndExit(VisualSignal signal) {
        Signal mathSignal = signal.getReferencedSignal();
        Color color = signal.getForegroundColor();

        EntryEvent mathEntry = new EntryEvent();
        mathSignal.add(mathEntry);
        VisualEntryEvent entry = new VisualEntryEvent(mathEntry);
        signal.add(entry);
        entry.setForegroundColor(color);

        ExitEvent mathExit = new ExitEvent();
        mathSignal.add(mathExit);
        VisualExitEvent exit = new VisualExitEvent(mathExit);
        signal.add(exit);
        exit.setForegroundColor(color);
        try {
            VisualConnection connection = connect(entry, exit);
            connection.setColor(color);
        } catch (InvalidConnectionException e) {
        }
    }

    public VisualTransitionEvent createVisualTransition(VisualSignal signal, TransitionEvent.Direction direction) {
        Signal mathSignal = signal.getReferencedSignal();
        TransitionEvent mathTransition = new TransitionEvent();
        if (direction != null) {
            mathTransition.setDirection(direction);
        }
        mathSignal.add(mathTransition);

        VisualTransitionEvent transition = new VisualTransitionEvent(mathTransition);
        signal.add(transition);
        return transition;
    }

    public SignalEvent appendSignalEvent(VisualSignal signal, TransitionEvent.Direction direction) {
        VisualEvent event = signal.getVisualSignalEntry();
        for (VisualTransitionEvent transition: signal.getVisualTransitions()) {
            if ((event == null) || (transition.getX() > event.getX())) {
                event = transition;
            }
        }
        if ((event instanceof VisualEntryEvent) && (signal.getInitialState() == Signal.State.STABLE)) {
            throw new RuntimeException("Signal at unknown state cannot change.");
        }
        if ((event instanceof VisualTransitionEvent) && (((VisualTransitionEvent) event).getDirection() == TransitionEvent.Direction.STABILISE)) {
            throw new RuntimeException("Signal at unknown state cannot change.");
        }
        VisualExitEvent exit = signal.getVisualSignalExit();
        Connection connection = getConnection(event, exit);
        if (connection != null) {
            remove(connection);
        }
        if (direction == null) {
            Signal.State state = DtdUtils.getNextState(event.getReferencedSignalEvent());
            direction = DtdUtils.getNextDirection(state);
        } else {
            if (event instanceof VisualEntryEvent) {
                Signal.State previousState = DtdUtils.getPreviousState(direction);
                if (previousState != null) {
                    signal.setInitialState(previousState);
                }
            }
        }
        VisualTransitionEvent edge = createVisualTransition(signal, direction);
        double x = signal.getX();
        double y = signal.getY();
        if (event != null) {
            x = event.getX();
        }
        double offset = DtdSettings.getTransitionSeparation();
        x += offset;
        if (x + offset > exit.getX()) {
            exit.setPosition(new Point2D.Double(x + offset, y));
        }
        edge.setPosition(new Point2D.Double(x, y));
        Color color = signal.getForegroundColor();
        edge.setForegroundColor(color);
        VisualConnection afterLevel = null;
        try {
            afterLevel = connect(edge, exit);
            afterLevel.setColor(color);
        } catch (InvalidConnectionException e) {
        }
        VisualConnection beforeLevel = null;
        try {
            beforeLevel = connect(event, edge);
            if (connection instanceof VisualLevelConnection) {
                color = ((VisualLevelConnection) connection).getColor();
            }
            beforeLevel.setColor(color);
        } catch (InvalidConnectionException e) {
        }
        return new SignalEvent(beforeLevel, edge, afterLevel);
    }

    @Override
    public void deleteSelection() {
        HashSet<Node> undeletableNodes = new HashSet<>();
        for (Node node: getSelection()) {
            if (node instanceof VisualEvent) {
                undeletableNodes.add(node);
            }
        }
        removeFromSelection(undeletableNodes);
        super.deleteSelection();
    }

}
