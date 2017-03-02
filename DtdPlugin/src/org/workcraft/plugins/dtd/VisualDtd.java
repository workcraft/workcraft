package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
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
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.dtd.Signal.State;
import org.workcraft.plugins.dtd.SignalTransition.Direction;
import org.workcraft.plugins.dtd.propertydescriptors.SignalInitialStatePropertyDescriptor;
import org.workcraft.plugins.dtd.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.util.Hierarchy;

@DisplayName("Digital Timing Diagram")
@CustomTools(DtdToolsProvider.class)
public class VisualDtd extends AbstractVisualModel {

    private static final double APPEND_EDGE_OFFSET = 0.5;
    private static final double INSERT_PULSE_OFFSET = 0.5;

    public class SignalEvent {
        public final VisualConnection beforeLevel;
        public final VisualSignalTransition edge;
        public final VisualConnection afterLevel;
        SignalEvent(VisualConnection beforeLevel, VisualSignalTransition edge, VisualConnection afterLevel) {
            this.beforeLevel = beforeLevel;
            this.edge = edge;
            this.afterLevel = afterLevel;
        }

        public boolean isValid() {
            return (beforeLevel != null) && (edge != null) && (afterLevel != null);
        }
    }

    public class SignalPulse {
        public final VisualConnection beforeLevel;
        public final VisualSignalTransition leadEdge;
        public final VisualConnection level;
        public final VisualSignalTransition trailEdge;
        public final VisualConnection afterLevel;
        SignalPulse(VisualConnection beforeLevel, VisualSignalTransition leadEdge,
                VisualConnection level, VisualSignalTransition trailEdge, VisualConnection afterLevel) {
            this.beforeLevel = beforeLevel;
            this.leadEdge = leadEdge;
            this.level = level;
            this.trailEdge = trailEdge;
            this.afterLevel = afterLevel;
        }

        public boolean isValid() {
            return (beforeLevel != null) && (leadEdge != null) && (level != null) && (trailEdge != null) && (afterLevel != null);
        }
    }

    public VisualDtd(Dtd model) {
        this(model, null);
    }

    public VisualDtd(Dtd model, VisualGroup root) {
        super(model, root);
        new DtdStateSupervisor(this).attach(getRoot());
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self-loops are not allowed.");
        }

        if (getConnection(first, second) != null) {
            throw new InvalidConnectionException("Connection already exists.");
        }

        if ((first instanceof VisualSignalTransition) && (second instanceof VisualSignalTransition)) {
            VisualSignalTransition firstTransition = (VisualSignalTransition) first;
            VisualSignalTransition secondTransition = (VisualSignalTransition) second;
            if (firstTransition.getX() > secondTransition.getX()) {
                throw new InvalidConnectionException("Invalid order of transitions.");
            }
            if (firstTransition.getParent() == secondTransition.getParent()) {
                if (firstTransition.getDirection() == Direction.STABILISE) {
                    throw new InvalidConnectionException("Signal at unknown state cannot change.");
                }
                if ((firstTransition.getDirection() != Direction.DESTABILISE)
                        && (secondTransition.getDirection() == Direction.STABILISE)) {
                    throw new InvalidConnectionException("Only unstable signal can stabilise.");
                }
                if (firstTransition.getDirection() == secondTransition.getDirection()) {
                    throw new InvalidConnectionException("Cannot connect transitions of the same signal and direction.");
                }
            }
            return;
        }

        if ((first instanceof VisualSignalEntry) && (second instanceof VisualSignalExit)) {
            VisualSignalEntry firstEntry = (VisualSignalEntry) first;
            VisualSignal firstSignal = firstEntry.getVisualSignal();
            VisualSignalExit secondExit = (VisualSignalExit) second;
            VisualSignal secondSignal = secondExit.getVisualSignal();
            if (firstSignal != secondSignal) {
                throw new InvalidConnectionException("Cannot relate entry and exit of different signals.");
            }
            return;
        }

        if ((first instanceof VisualSignalEntry) && (second instanceof VisualSignalTransition)) {
            VisualSignalEntry firstEntry = (VisualSignalEntry) first;
            VisualSignal firstSignal = firstEntry.getVisualSignal();
            VisualSignalTransition secondTransition = (VisualSignalTransition) second;
            VisualSignal secondSignal = secondTransition.getVisualSignal();
            if (firstSignal != secondSignal) {
                throw new InvalidConnectionException("Cannot relate entry and transition of different signals.");
            }
            if (firstSignal.getInitialState() == State.STABLE) {
                throw new InvalidConnectionException("Signal at unknown state cannot change.");
            }
            if ((firstSignal.getInitialState() != State.UNSTABLE)
                    && (secondTransition.getDirection() == Direction.STABILISE)) {
                throw new InvalidConnectionException("Only unstable signal can stabilise.");
            }
            if ((firstSignal.getInitialState() == State.HIGH)
                    && (secondTransition.getDirection() == Direction.RISE)) {
                throw new InvalidConnectionException("Signal is already high.");
            }
            if ((firstSignal.getInitialState() == State.LOW)
                    && (secondTransition.getDirection() == Direction.FALL)) {
                throw new InvalidConnectionException("Signal is already low.");
            }
            return;
        }

        if ((first instanceof VisualSignalTransition) && (second instanceof VisualSignalExit)) {
            VisualSignalTransition firstTransition = (VisualSignalTransition) first;
            VisualSignal firstSignal = firstTransition.getVisualSignal();
            VisualSignalExit secondExit = (VisualSignalExit) second;
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

        if (mConnection == null) {
            mConnection = ((Dtd) getMathModel()).connect(m1, m2);
        }

        Container container = Hierarchy.getNearestContainer(v1, v2);
        VisualConnection vConnection;
        boolean isLevelConnection = DtdUtils.isLevelConnection(mConnection);
        boolean isEventConnection = DtdUtils.isEventConnection(mConnection);
        if (isLevelConnection) {
            vConnection = new VisualLevelConnection(mConnection, v1, v2);
        } else {
            vConnection = new VisualConnection(mConnection, v1, v2);
        }
        container.add(vConnection);
        if (isLevelConnection) {
            DtdUtils.decorateVisualLevelConnection(vConnection);
        } else if (isEventConnection) {
            DtdUtils.decorateVisualEventConnection(vConnection);
        }
        return vConnection;
    }

    public Collection<VisualSignal> getVisualSignals(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getChildrenOfType(container, VisualSignal.class);
    }

    public Collection<VisualSignalTransition> getVisualSignalTransitions(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, VisualSignalTransition.class);
    }

    public Collection<VisualSignalEntry> getVisualSignalEntries(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, VisualSignalEntry.class);
    }

    public Collection<VisualSignalExit> getVisualSignalExits(Container container) {
        if (container == null) {
            container = getRoot();
        }
        return Hierarchy.getDescendantsOfType(container, VisualSignalExit.class);
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

        SignalEntry mathEntry = new SignalEntry();
        mathSignal.add(mathEntry);
        VisualSignalEntry entry = new VisualSignalEntry(mathEntry);
        signal.add(entry);
        entry.setPosition(new Point2D.Double(0.5, 0.0));

        SignalExit mathExit = new SignalExit();
        mathSignal.add(mathExit);
        VisualSignalExit exit = new VisualSignalExit(mathExit);
        signal.add(exit);
        exit.setPosition(new Point2D.Double(2.0, 0.0));
        try {
            connect(entry, exit);
        } catch (InvalidConnectionException e) {
        }
    }

    public VisualSignalTransition createVisualTransition(VisualSignal signal, Direction direction) {
        Signal mathSignal = signal.getReferencedSignal();
        SignalTransition mathTransition = new SignalTransition();
        if (direction != null) {
            mathTransition.setDirection(direction);
        }
        mathSignal.add(mathTransition);

        VisualSignalTransition transition = new VisualSignalTransition(mathTransition);
        signal.add(transition);
        return transition;
    }

    public SignalEvent appendSignalEvent(VisualSignal signal, Direction direction) {
        VisualSignalEvent event = signal.getVisualSignalEntry();
        for (VisualSignalTransition transition: signal.getVisualTransitions()) {
            if ((event == null) || (transition.getX() > event.getX())) {
                event = transition;
            }
        }
        VisualSignalExit exit = signal.getVisualSignalExit();
        Connection connection = getConnection(event, exit);
        if (connection != null) {
            remove(connection);
        }
        State state = signal.getInitialState();
        if (direction == null) {
            state = DtdUtils.getNextState(event.getReferencedSignalEvent());
            direction = DtdUtils.getNextDirection(state);
        } else {
            if (event instanceof VisualSignalEntry) {
                State previousState = DtdUtils.getPreviousState(direction);
                if (previousState != null) {
                    signal.setInitialState(previousState);
                }
            }
        }
        VisualSignalTransition edge = createVisualTransition(signal, direction);
        double x = signal.getX();
        double y = signal.getY();
        if (event != null) {
            x = event.getX();
        }
        x += APPEND_EDGE_OFFSET;
        if ((exit != null) && (x + APPEND_EDGE_OFFSET > exit.getX())) {
            exit.setPosition(new Point2D.Double(x + APPEND_EDGE_OFFSET, y));
        }
        edge.setPosition(new Point2D.Double(x, y));
        VisualConnection beforeLevel = null;
        try {
            beforeLevel = connect(event, edge);
            beforeLevel.setColor(signal.getForegroundColor());
        } catch (InvalidConnectionException e) {
        }
        VisualConnection afterLevel = null;
        try {
            afterLevel = connect(edge, exit);
            afterLevel.setColor(signal.getForegroundColor());
        } catch (InvalidConnectionException e) {
        }
        return new SignalEvent(beforeLevel, edge, afterLevel);
    }

    public SignalPulse insertSignalPulse(VisualLevelConnection connection) {
        VisualSignalEvent fromEvent = (VisualSignalEvent) connection.getFirst();
        VisualSignalEvent toEvent = (VisualSignalEvent) connection.getSecond();
        State state = DtdUtils.getNextState(fromEvent.getReferencedSignalEvent());
        VisualSignal signal = fromEvent.getVisualSignal();
        Direction leadDirection = DtdUtils.getPreviousDirection(state);
        Direction trailDirection = DtdUtils.getNextDirection(state);
        VisualSignalTransition leadEdge = createVisualTransition(signal, leadDirection);
        VisualSignalTransition trailEdge = createVisualTransition(signal, trailDirection);

        double y = fromEvent.getY();
        Point2D p = connection.getMiddleSegmentCenterPoint();
        double leadX = (p.getX() - INSERT_PULSE_OFFSET < fromEvent.getX())
                ? 0.5 * (p.getX() + fromEvent.getX()) : p.getX() - 0.5 * INSERT_PULSE_OFFSET;
        double trailX = (p.getX() + INSERT_PULSE_OFFSET > toEvent.getX())
                ? 0.5 * (p.getX() + toEvent.getRootSpaceX()) : p.getX() + 0.5 * INSERT_PULSE_OFFSET;
        leadEdge.setRootSpacePosition(new Point2D.Double(leadX, y));
        trailEdge.setRootSpacePosition(new Point2D.Double(trailX, y));

        remove(connection);
        VisualConnection leadLevel = null;
        VisualConnection midLevel = null;
        VisualConnection trailLevel = null;
        try {
            leadLevel = connect(fromEvent, leadEdge);
            midLevel = connect(leadEdge, trailEdge);
            trailLevel = connect(trailEdge, toEvent);
        } catch (InvalidConnectionException e) {
        }
        return new SignalPulse(leadLevel, leadEdge, midLevel, trailEdge, trailLevel);
    }

    @Override
    public void deleteSelection() {
        HashSet<Node> undeletableNodes = new HashSet<>();
        for (Node node: getSelection()) {
            if ((node instanceof VisualSignalEntry) || (node instanceof VisualSignalExit)) {
                undeletableNodes.add(node);
            }
        }
        removeFromSelection(undeletableNodes);
        super.deleteSelection();
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            for (final VisualSignal signal: getVisualSignals(getCurrentLevel())) {
                Signal mathSignal = signal.getReferencedSignal();
                SignalTypePropertyDescriptor typeDescriptor = new SignalTypePropertyDescriptor(getMathModel(), mathSignal);
                properties.insertOrderedByFirstWord(typeDescriptor);
                SignalInitialStatePropertyDescriptor initialStateDescriptor = new SignalInitialStatePropertyDescriptor(getMathModel(), mathSignal);
                properties.insertOrderedByFirstWord(initialStateDescriptor);
            }
        } else if (node instanceof VisualSignalTransition) {
            properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
        }
        return properties;
    }

}
