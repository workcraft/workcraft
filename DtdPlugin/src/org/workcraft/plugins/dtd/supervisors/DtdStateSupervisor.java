package org.workcraft.plugins.dtd.supervisors;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.observation.*;
import org.workcraft.plugins.dtd.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class DtdStateSupervisor extends StateSupervisor {

    private static final double OFFSET_ENTRY = 0.0;
    private static final double OFFSET_EXIT = 1.0;

    private final VisualDtd dtd;

    public DtdStateSupervisor(VisualDtd dtd) {
        this.dtd = dtd;
    }

    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        Object sender = e.getSender();
        if ((e instanceof NodesAddedEvent) && (sender instanceof VisualSignal)) {
            for (Node node : e.getAffectedNodes()) {
                if (node instanceof VisualEntryEvent) {
                    handleSignalEntryCreation((VisualSignal) sender, (VisualEntryEvent) node);
                } else if (node instanceof VisualExitEvent) {
                    handleSignalExitCreation((VisualSignal) sender, (VisualExitEvent) node);
                }
            }
        }
    }

    private void handleSignalEntryCreation(VisualSignal signal, VisualEntryEvent entry) {
        entry.setPosition(new Point2D.Double(OFFSET_ENTRY, 0.0));
    }

    private void handleSignalExitCreation(VisualSignal signal, VisualExitEvent exit) {
        // Offset exit event to align with the furthest one
        Container container = (Container) signal.getParent();
        double x = OFFSET_EXIT;
        for (VisualExitEvent otherExit : dtd.getVisualSignalExits(container)) {
            if (otherExit.getX() > x) {
                x = otherExit.getX();
            }
        }
        if (exit.getX() != x) {
            exit.setX(x);
        }
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
                handleSignalInitialStateChange(signal);
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
        if (entry.getX() != 0.0) {
            entry.setX(OFFSET_ENTRY);
        }
        if (entry.getY() != 0.0) {
            entry.setY(0.0);
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
                // Align other exit events to this one
                Container container = (Container) signal.getParent();
                for (VisualExitEvent otherExit : dtd.getVisualSignalExits(container)) {
                    if (exit.getX() != otherExit.getX()) {
                        otherExit.setX(exit.getX());
                    }
                }
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

    private void handleSignalInitialStateChange(VisualSignal signal) {
        VisualEntryEvent entry = signal.getVisualSignalEntry();
        VisualEvent nextEvent = DtdUtils.getNextVisualEvent(dtd, entry);
        boolean done = false;
        if (nextEvent instanceof VisualTransitionEvent) {
            VisualTransitionEvent nextTransition = (VisualTransitionEvent) nextEvent;
            Signal.State state = signal.getInitialState();
            if (DtdUtils.getPreviousDirection(state) == nextTransition.getDirection()) {
                DtdUtils.removeTransitionEvent(dtd, nextTransition);
                done = true;
            } else if (state == Signal.State.STABLE) {
                DtdUtils.removeSuffixTransitionEvents(dtd, entry);
                done = true;
            }
        }
        if (!done) {
            Connection connection = dtd.getConnection(entry, nextEvent);
            if (connection instanceof VisualLevelConnection) {
                dtd.removeFromSelection(connection);
                dtd.remove(connection);
                try {
                    dtd.connect(entry, nextEvent);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleTransitionDirectionChange(VisualTransitionEvent transition) {
        boolean nextDone = false;
        if (transition.getDirection() == TransitionEvent.Direction.STABILISE) {
            DtdUtils.removePrefixTransitionEvents(dtd, transition, TransitionEvent.Direction.DESTABILISE);
            DtdUtils.removeSuffixTransitionEvents(dtd, transition);
            nextDone = true;
        }
        VisualEvent nextEvent = DtdUtils.getNextVisualEvent(dtd, transition);
        if (nextEvent instanceof VisualTransitionEvent) {
            VisualTransitionEvent nextTransition = (VisualTransitionEvent) nextEvent;
            if (transition.getDirection() == nextTransition.getDirection()) {
                DtdUtils.removeTransitionEvent(dtd, nextTransition);
                nextDone = true;
            }
        }
        if (!nextDone) {
            Connection connection = dtd.getConnection(transition, nextEvent);
            if (connection instanceof VisualLevelConnection) {
                dtd.removeFromSelection(connection);
                dtd.remove(connection);
                try {
                    dtd.connect(transition, nextEvent);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        boolean predDone = false;
        VisualEvent predEvent = DtdUtils.getPredVisualEvent(dtd, transition);
        if (predEvent instanceof VisualTransitionEvent) {
            VisualTransitionEvent predTransition = (VisualTransitionEvent) predEvent;
            if (predTransition.getDirection() == transition.getDirection()) {
                DtdUtils.removeTransitionEvent(dtd, transition);
                predDone = true;
            }
        }
        if (predEvent instanceof VisualEntryEvent) {
            VisualSignal signal = transition.getVisualSignal();
            Signal.State state = signal.getInitialState();
            if (DtdUtils.getPreviousDirection(state) == transition.getDirection()) {
                DtdUtils.removeTransitionEvent(dtd, transition);
                predDone = true;
            }
        }
        if (!predDone) {
            Connection connection = dtd.getConnection(predEvent, transition);
            if (connection instanceof VisualLevelConnection) {
                dtd.removeFromSelection(connection);
                dtd.remove(connection);
                try {
                    dtd.connect(predEvent, transition);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
