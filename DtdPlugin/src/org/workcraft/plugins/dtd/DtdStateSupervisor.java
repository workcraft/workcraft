package org.workcraft.plugins.dtd;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.observation.TransformChangedEvent;

public final class DtdStateSupervisor extends StateSupervisor {

    private final VisualDtd dtd;

    DtdStateSupervisor(VisualDtd dtd) {
        this.dtd = dtd;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof TransformChangedEvent) {
            TransformChangedEvent tce = (TransformChangedEvent) e;
            if (tce.getSender() instanceof VisualTransition) {
                VisualTransition transition = (VisualTransition) tce.getSender();
                handleTransitionTransformation(transition);
                handleComponentTransformation(transition);
            } else if (tce.getSender() instanceof VisualSignal) {
                VisualSignal signal = (VisualSignal) tce.getSender();
                handleSignalTransformation(signal);
                handleComponentTransformation(signal);
            }
        } else if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            String propertyName = pce.getPropertyName();
            if ((pce.getSender() instanceof Transition) && (propertyName.equals(Transition.PROPERTY_DIRECTION))) {
                VisualTransition transtition = dtd.getVisualComponent((Transition) pce.getSender(), VisualTransition.class);
                handleTransitionChangeDirection(transtition);
            }
        }
    }

    private void handleTransitionTransformation(VisualTransition transition) {
        VisualSignal signal = dtd.getVisualSignal(transition);
        if (signal != null) {
            double y = signal.getRootSpaceY();
            align(transition, y);
        }
    }

    private void handleSignalTransformation(VisualSignal signal) {
        double y = signal.getRootSpaceY();
        for (VisualTransition transition: dtd.getVisualTransitions(signal)) {
            align(transition, y);
        }
    }

    private void align(VisualTransition transition, double y) {
        double d = Math.abs(y - transition.getRootSpaceY());
        if (d > 0.001) {
            transition.setRootSpaceY(y);
        }
    }

    private void handleComponentTransformation(VisualComponent component) {
        Rectangle2D bb = component.getBoundingBox();
        double xMin = bb.getMinX();
        for (Node predNode: dtd.getPreset(component)) {
            VisualComponent predComponent = (VisualComponent) predNode;
            xMin = Math.max(xMin, predComponent.getBoundingBox().getMaxX());
        }
        double xMax = bb.getMaxX();
        for (Node succNode: dtd.getPostset(component)) {
            VisualComponent succComponent = (VisualComponent) succNode;
            xMax = Math.min(xMax, succComponent.getBoundingBox().getMinX());
        }
        if (xMin > bb.getMinX()) {
            double xOffset = component.getX() - bb.getMinX();
            component.setX(xMin + xOffset);
        } else if (xMax < bb.getMaxX()) {
            double xOffset = bb.getMaxX() - component.getX();
            component.setX(xMax - xOffset);
        }
    }

    private void handleTransitionChangeDirection(VisualTransition transition) {
        Set<Connection> connections = new HashSet<>(dtd.getConnections(transition));
        for (Connection connection: connections) {
            if (connection instanceof VisualLevelConnection) {
                Node first = connection.getFirst();
                Node second = connection.getSecond();
                dtd.remove(connection);
                try {
                    dtd.connect(first, second);
                } catch (InvalidConnectionException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
