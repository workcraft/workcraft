/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.plugins.dtd.Transition.Direction;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

@DisplayName("Digital Timing Diagram")
@CustomTools(DtdToolsProvider.class)
public class VisualDtd extends AbstractVisualModel {

    public VisualDtd(Dtd model) {
        this(model, null);
    }

    public VisualDtd(Dtd model, VisualGroup root) {
        super(model, root);
        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }

        new DtdStateSupervisor(this).attach(getRoot());
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self loops are not allowed.");
        }

        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) {
            Transition firstTransition = ((VisualTransition) first).getReferencedTransition();
            Transition secondTransition = ((VisualTransition) second).getReferencedTransition();
            if ((firstTransition.getSignal() == secondTransition.getSignal())
                    && (firstTransition.getDirection() == secondTransition.getDirection())) {
                throw new InvalidConnectionException("Cannot order transitions of the same signal and direction.");
            }
            return;
        }

        if ((first instanceof VisualSignal) && (second instanceof VisualTransition)) {
            Signal firstSignal = ((VisualSignal) first).getReferencedSignal();
            Transition secondTransition = ((VisualTransition) second).getReferencedTransition();
            if (firstSignal != secondTransition.getSignal()) {
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
        VisualConnection vConnection = new VisualConnection(mConnection, v1, v2);
        Container container = Hierarchy.getNearestContainer(v1, v2);
        container.add(vConnection);
        if ((v1 instanceof VisualSignal) && (v2 instanceof VisualTransition)) {
            Signal s1 = ((VisualSignal) v1).getReferencedSignal();
            Transition t2 = ((VisualTransition) v2).getReferencedTransition();
            if (s1 == t2.getSignal()) {
                vConnection.setConnectionType(ConnectionType.POLYLINE);
                Polyline polyline = (Polyline) vConnection.getGraphic();
                vConnection.setArrow(false);
                vConnection.setLineWidth(CommonVisualSettings.getStrokeWidth());
                vConnection.setScaleMode(ScaleMode.LOCK_RELATIVELY);

                double size = CommonVisualSettings.getBaseSize();
                double offset = size * ((t2.getDirection() == Direction.MINUS) ? -0.5 : 0.5);
                Point2D cp1 = new Point2D.Double(v1.getX(), v1.getY() + offset);
                Point2D cp2 = new Point2D.Double(v2.getX(), v2.getY() + offset);
                polyline.addControlPoint(cp1);
                polyline.addControlPoint(cp2);
            }
        } else if ((v1 instanceof VisualTransition) && (v2 instanceof VisualTransition)) {
            Transition t1 = ((VisualTransition) v1).getReferencedTransition();
            Transition t2 = ((VisualTransition) v2).getReferencedTransition();
            if (t1.getSignal() == t2.getSignal()) {
                vConnection.setConnectionType(ConnectionType.POLYLINE);
                Polyline polyline = (Polyline) vConnection.getGraphic();
                vConnection.setArrow(false);
                vConnection.setLineWidth(CommonVisualSettings.getStrokeWidth());
                vConnection.setScaleMode(ScaleMode.LOCK_RELATIVELY);

                double size = CommonVisualSettings.getBaseSize();
                double offset1 = size * ((t1.getDirection() == Direction.PLUS) ? -0.5 : 0.5);
                Point2D cp1 = new Point2D.Double(v1.getX(), v1.getY() + offset1);
                polyline.addControlPoint(cp1);

                double offset2 = size * ((t2.getDirection() == Direction.MINUS) ? -0.5 : 0.5);
                Point2D cp2 = new Point2D.Double(v2.getX(), v2.getY() + offset2);
                polyline.addControlPoint(cp2);
            } else {
                vConnection.setConnectionType(ConnectionType.BEZIER);
                Bezier bezier = (Bezier) vConnection.getGraphic();
                BezierControlPoint[] cp = bezier.getBezierControlPoints();
                cp[0].setRootSpacePosition(new Point2D.Double(v1.getX() + 2.0, v1.getY()));
                cp[1].setRootSpacePosition(new Point2D.Double(v2.getX() - 2.0, v2.getY()));
            }
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

    public VisualTransition createVisualTransition(VisualSignal signal) {
        Transition mathTransition = new Transition(signal.getReferencedSignal());
        getMathModel().add(mathTransition);
        VisualTransition visualTransition = new VisualTransition(mathTransition);
        add(visualTransition);
        return visualTransition;
    }

    public VisualTransition appendVisualTransition(VisualSignal signal) {
        double xMax = signal.getX();
        boolean first = true;
        Direction direction = Direction.PLUS;
        for (VisualTransition transition: getVisualTransitions(signal)) {
            if (first || (transition.getX() > xMax)) {
                xMax = transition.getX();
                direction = transition.getReferencedTransition().getDirection();
                first = false;
            }
        }
        VisualTransition result = createVisualTransition(signal);
        result.setPosition(new Point2D.Double(xMax + 5.0, signal.getY()));
        result.getReferencedTransition().setDirection(direction.reverse());
        return result;
    }

}
