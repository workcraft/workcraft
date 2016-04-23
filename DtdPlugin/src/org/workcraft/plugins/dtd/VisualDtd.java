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
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.observation.TransformChangedEvent;
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

        new StateSupervisor() {
            @Override
            public void handleEvent(StateEvent e) {
                if (e instanceof TransformChangedEvent) {
                    TransformChangedEvent tce = (TransformChangedEvent) e;
                    if (tce.getSender() instanceof VisualTransition) {
                        VisualTransition transition = (VisualTransition) tce.getSender();
                        VisualSignal signal = getVisualSignal(transition);
                        if (signal != null) {
                            if (signal.getY() != transition.getY()) {
                                transition.setY(signal.getY());
                            }
                        }
                    } else if (tce.getSender() instanceof VisualSignal) {
                        VisualSignal signal = (VisualSignal) tce.getSender();
                        for (VisualTransition transition: getVisualTransitions(signal)) {
                            if (transition.getY() != signal.getY()) {
                                transition.setY(signal.getY());
                            }
                        }
                    }
                }
            }
        }.attach(getRoot());
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

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self loops are not allowed.");
        }

        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) return;

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
        return vConnection;
    }

    public Collection<VisualSignal> getVisualSignals() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualSignal.class);
    }

    public Collection<VisualTransition> getVisualTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualTransition.class);
    }

}
