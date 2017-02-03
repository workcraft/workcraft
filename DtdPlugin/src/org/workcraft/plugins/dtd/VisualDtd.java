package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.util.Hierarchy;

@DisplayName("Digital Timing Diagram")
@CustomTools(DtdToolsProvider.class)
public class VisualDtd extends VisualGraph {

    private static final double APPEND_EDGE_OFFSET = 2.0;
    private static final double INSERT_PULSE_OFFSET = 1.0;

    public class SignalEvent {
        public final VisualConnection level;
        public final VisualTransition edge;
        SignalEvent(VisualConnection level, VisualTransition edge) {
            this.level = level;
            this.edge = edge;
        }

        public boolean isValid() {
            return (level != null) && (edge != null);
        }
    }

    public class SignalPulse {
        public final VisualConnection leadLevel;
        public final VisualTransition leadEdge;
        public final VisualConnection midLevel;
        public final VisualTransition trailEdge;
        public final VisualConnection trailLevel;
        SignalPulse(VisualConnection leadLevel, VisualTransition leadEdge, VisualConnection midLevel, VisualTransition trailEdge, VisualConnection trailLevel) {
            this.leadLevel = leadLevel;
            this.leadEdge = leadEdge;
            this.midLevel = midLevel;
            this.trailEdge = trailEdge;
            this.trailLevel = trailLevel;
        }

        public boolean isValid() {
            return (leadLevel != null) && (leadEdge != null) && (midLevel != null) && (trailEdge != null) && (trailLevel != null);
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

        if ((first instanceof VisualComponent) && (second instanceof VisualComponent)) {
            VisualComponent firstComponent = (VisualComponent) first;
            VisualComponent secondComponent = (VisualComponent) second;
            if (firstComponent.getRootSpaceX() > secondComponent.getRootSpaceX()) {
                throw new InvalidConnectionException("Invalid order of events.");
            }
        }

        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) {
            VisualTransition firstTransition = (VisualTransition) first;
            VisualTransition secondTransition = (VisualTransition) second;
            if ((firstTransition.getSignal() == secondTransition.getSignal())
                    && (firstTransition.getDirection() == secondTransition.getDirection())) {
                throw new InvalidConnectionException("Cannot order transitions of the same signal and direction.");
            }
            return;
        }

        if ((first instanceof VisualSignal) && (second instanceof VisualTransition)) {
            Signal firstSignal = ((VisualSignal) first).getReferencedSignal();
            Signal secondSignal = ((VisualTransition) second).getSignal();
            if (firstSignal != secondSignal) {
                throw new InvalidConnectionException("Cannot relate transition with a different signal.");
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
        if (DtdUtils.isLevelConnection(mConnection)) {
            vConnection = new VisualLevelConnection(mConnection, v1, v2);
            container.add(vConnection);
            DtdUtils.decorateVisualLevelConnection(vConnection);
        } else {
            vConnection = new VisualConnection(mConnection, v1, v2);
            container.add(vConnection);
            DtdUtils.decorateVisualEventConnection(vConnection);
        }
        return vConnection;
    }

    public Collection<VisualSignal> getVisualSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualSignal.class);
    }

    public Collection<VisualTransition> getVisualTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualTransition.class);
    }

    protected VisualSignal getVisualSignal(VisualTransition transition) {
        for (VisualSignal signal: getVisualSignals()) {
            Signal refSignal = transition.getReferencedTransition().getSignal();
            if (signal.getReferencedSignal() == refSignal) {
                return signal;
            }
        }
        return null;
    }

    protected Collection<VisualTransition> getVisualTransitions(VisualSignal signal) {
        HashSet<VisualTransition> result = new HashSet<>();
        Signal refSignal = signal.getReferencedSignal();
        for (VisualTransition transition: getVisualTransitions()) {
            if (transition.getReferencedTransition().getSignal() == refSignal) {
                result.add(transition);
            }
        }
        return result;
    }

    public VisualSignal createVisualSignal(String name) {
        Signal mathSignal = new Signal();
        getMathModel().add(mathSignal);
        getMathModel().setName(mathSignal, name);
        VisualSignal visualSignal = new VisualSignal(mathSignal);
        add(visualSignal);
        return visualSignal;
    }

    public VisualTransition createVisualTransition(VisualSignal signal, Direction direction) {
        Signal mathSignal = signal.getReferencedSignal();
        Transition mathTransition = new Transition(mathSignal);
        if (direction != null) {
            mathTransition.setDirection(direction);
        }
        Container mathContainer = Hierarchy.getNearestContainer(mathSignal);
        mathContainer.add(mathTransition);

        VisualTransition transition = new VisualTransition(mathTransition);
        Container container = Hierarchy.getNearestContainer(signal);
        transition.setForegroundColor(signal.getForegroundColor());
        container.add(transition);
        return transition;
    }

    public SignalEvent appendSignalEvent(VisualSignal signal, Direction direction) {
        VisualTransition lastTransition = null;
        for (VisualTransition transition: getVisualTransitions(signal)) {
            if ((lastTransition == null) || (transition.getRootSpaceX() > lastTransition.getRootSpaceX())) {
                lastTransition = transition;
            }
        }
        VisualComponent fromComponent = (lastTransition != null) ? lastTransition : signal;
        if ((direction == null) && (lastTransition != null)) {
            direction = lastTransition.getDirection().reverse();
        }
        VisualTransition edge = createVisualTransition(signal, direction);
        Point2D pos = new Point2D.Double(fromComponent.getRootSpaceX() + APPEND_EDGE_OFFSET, fromComponent.getRootSpaceY());
        edge.setRootSpacePosition(pos);
        VisualConnection level = null;
        try {
            level = connect(fromComponent, edge);
            level.setColor(signal.getForegroundColor());
        } catch (InvalidConnectionException e) {
        }
        return new SignalEvent(level, edge);
    }

    public SignalPulse insetrSignalPulse(VisualLevelConnection connection) {
        VisualComponent fromComponent = (VisualComponent) connection.getFirst();
        VisualTransition toTransition = (VisualTransition) connection.getSecond();
        VisualSignal signal = getVisualSignal(toTransition);
        Direction direction = toTransition.getDirection();
        VisualTransition leadEdge = createVisualTransition(signal, direction);
        VisualTransition trailEdge = createVisualTransition(signal, direction.reverse());

        double y = fromComponent.getY();
        Point2D p = connection.getMiddleSegmentCenterPoint();
        double leadX = (p.getX() - INSERT_PULSE_OFFSET < fromComponent.getRootSpaceX())
                ? 0.5 * (p.getX() + fromComponent.getRootSpaceX()) : p.getX() - 0.5 * INSERT_PULSE_OFFSET;
        double trailX = (p.getX() + INSERT_PULSE_OFFSET > toTransition.getRootSpaceX())
                ? 0.5 * (p.getX() + toTransition.getRootSpaceX()) : p.getX() + 0.5 * INSERT_PULSE_OFFSET;
        leadEdge.setRootSpacePosition(new Point2D.Double(leadX, y));
        trailEdge.setRootSpacePosition(new Point2D.Double(trailX, y));

        remove(connection);
        VisualConnection leadLevel = null;
        VisualConnection midLevel = null;
        VisualConnection trailLevel = null;
        try {
            leadLevel = connect(fromComponent, leadEdge);
            midLevel = connect(leadEdge, trailEdge);
            trailLevel = connect(trailEdge, toTransition);
        } catch (InvalidConnectionException e) {
        }
        return new SignalPulse(leadLevel, leadEdge, midLevel, trailEdge, trailLevel);
    }

}
