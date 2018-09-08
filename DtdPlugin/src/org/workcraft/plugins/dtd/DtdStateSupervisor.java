package org.workcraft.plugins.dtd;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
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
        Object sender = e.getSender();
        if (e instanceof TransformChangedEvent) {
            if (sender instanceof VisualSignal) {
                handleSignalTransformation((VisualSignal) sender);
            } else if (sender instanceof VisualTransitionEvent) {
                handleSignalTransitionTransformation((VisualTransitionEvent) sender);
            } else if (sender instanceof VisualEntryEvent) {
                handleSignalEntryTransformation((VisualEntryEvent) sender);
            } else if (sender instanceof VisualExitEvent) {
                handleSignalExitTransformation((VisualExitEvent) sender);
            }
        } else if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            String propertyName = pce.getPropertyName();
            if ((sender instanceof Signal) && (propertyName.equals(Signal.PROPERTY_INITIAL_STATE))) {
                VisualSignal signal = dtd.getVisualComponent((Signal) sender, VisualSignal.class);
                handleSignalStateChange(signal);
            } else if ((sender instanceof TransitionEvent) && (propertyName.equals(TransitionEvent.PROPERTY_DIRECTION))) {
                VisualTransitionEvent transtition = dtd.getVisualComponent((TransitionEvent) sender, VisualTransitionEvent.class);
                handleTransitionDirectionChange(transtition);
            }
        }
    }

    private void handleSignalTransformation(VisualSignal signal) {
        if (signal.getX() != 0.0) {
            signal.setX(0.0);
        }
    }

    private void handleSignalTransitionTransformation(VisualTransitionEvent transition) {
        VisualSignal signal = transition.getVisualSignal();
        if (signal != null) {
            VisualEntryEvent entry = signal.getVisualSignalEntry();
            VisualExitEvent exit = signal.getVisualSignalExit();
            if ((entry != null) && (exit != null)) {
                Rectangle2D bbEntry = entry.getBoundingBox();
                double xMin = bbEntry.getMinX();
                for (Node predNode: dtd.getPreset(transition)) {
                    VisualComponent predComponent = (VisualComponent) predNode;
                    xMin = Math.max(xMin, predComponent.getBoundingBox().getMaxX());
                }
                Rectangle2D bbExit = exit.getBoundingBox();
                double xMax = bbExit.getMaxX();
                for (Node succNode: dtd.getPostset(transition)) {
                    VisualComponent succComponent = (VisualComponent) succNode;
                    xMax = Math.min(xMax, succComponent.getBoundingBox().getMinX());
                }
                limitSignalEventPosition(transition, xMin, xMax);
            }
        }
    }

    private void handleSignalEntryTransformation(VisualEntryEvent entry) {
        VisualSignal signal = entry.getVisualSignal();
        if (signal != null) {
            Rectangle2D bbSignal = BoundingBoxHelper.union(signal.getNameBoundingBox(), signal.getLabelBoundingBox());
            if (bbSignal != null) {
                double xMin = bbSignal.getMaxX();
                VisualExitEvent exit = signal.getVisualSignalExit();
                if (exit != null) {
                    Rectangle2D bbExit = exit.getBoundingBox();
                    double xMax = bbExit.getMaxX();
                    for (VisualTransitionEvent transition: signal.getVisualTransitions()) {
                        xMax = Math.min(xMax, transition.getBoundingBox().getMinX());
                    }
                    limitSignalEventPosition(entry, xMin, xMax);
                }
            }
        }
    }

    private void handleSignalExitTransformation(VisualExitEvent exit) {
        VisualSignal signal = exit.getVisualSignal();
        if (signal != null) {
            VisualEntryEvent entry = signal.getVisualSignalEntry();
            if (entry != null) {
                Rectangle2D bbEntry = entry.getBoundingBox();
                double xMin = bbEntry.getMinX();
                for (VisualTransitionEvent transition: signal.getVisualTransitions()) {
                    xMin = Math.max(xMin, transition.getBoundingBox().getMaxX());
                }
                double xMax = xMin + 100.0;
                limitSignalEventPosition(exit, xMin, xMax);
            }
        }
    }

    private void limitSignalEventPosition(VisualEvent event, double xMin, double xMax) {
        Rectangle2D bb = event.getBoundingBox();
        double x = event.getX();
        if (xMin > bb.getMinX()) {
            double xOffset = event.getX() - bb.getMinX();
            x = xMin + xOffset;
        } else if (xMax < bb.getMaxX()) {
            double xOffset = bb.getMaxX() - event.getX();
            x = xMax - xOffset;
        }
        Point2D pos = new Point2D.Double(x, 0.0);
        if (pos.distance(event.getPosition()) > 0.001) {
            event.setPosition(pos);
        }
    }

    private void handleTransitionDirectionChange(VisualTransitionEvent transition) {
        Set<Connection> connections = new HashSet<>(dtd.getConnections(transition));
        for (Connection connection: connections) {
            if (connection instanceof VisualLevelConnection) {
                Node first = connection.getFirst();
                Node second = connection.getSecond();
                dtd.remove(connection);
                try {
                    dtd.connect(first, second);
                } catch (InvalidConnectionException e) {
                }
            }
        }
    }

    private void handleSignalStateChange(VisualSignal signal) {
        VisualEntryEvent entry = signal.getVisualSignalEntry();
        Set<Connection> connections = new HashSet<>(dtd.getConnections(entry));
        for (Connection connection: connections) {
            if (connection instanceof VisualLevelConnection) {
                Node first = connection.getFirst();
                Node second = connection.getSecond();
                dtd.remove(connection);
                try {
                    dtd.connect(first, second);
                } catch (InvalidConnectionException e) {
                }
            }
        }
    }

}
