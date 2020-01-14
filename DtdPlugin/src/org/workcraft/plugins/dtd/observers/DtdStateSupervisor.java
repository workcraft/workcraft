package org.workcraft.plugins.dtd.observers;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.observation.*;
import org.workcraft.plugins.dtd.*;
import org.workcraft.plugins.dtd.utils.DtdUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public final class DtdStateSupervisor extends StateSupervisor {

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
                    handleSignalEntryCreation((VisualEntryEvent) node);
                } else if (node instanceof VisualExitEvent) {
                    handleSignalExitCreation((VisualSignal) sender, (VisualExitEvent) node);
                }
            }
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
            } else if ((sender instanceof VisualSignal) && (propertyName.equals(VisualSignal.PROPERTY_COLOR))) {
                handleSignalColorChange((VisualSignal) sender);
            }
        }
    }

    private void handleSignalEntryCreation(VisualEntryEvent entry) {
        entry.setPosition(new Point2D.Double(0.0, 0.0));
    }

    private void handleSignalExitCreation(VisualSignal signal, VisualExitEvent exit) {
        // Offset exit event to align with the furthest one
        Container container = (Container) signal.getParent();
        double x = DtdSettings.getTransitionSeparation();
        for (VisualExitEvent otherExit : dtd.getVisualSignalExits(container)) {
            if (otherExit.getX() > x) {
                x = otherExit.getX();
            }
        }
        if (exit.getX() != x) {
            exit.setX(x);
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
            entry.setX(0.0);
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
                double xMax = xMin + 1000.0;
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
        if (bb.getMinX() < xMin) {
            x = xMin + event.getX() - bb.getMinX();
        } else if (bb.getMaxX() > xMax) {
            x = xMax - bb.getMaxX() + event.getX();
        }
        Point2D pos = new Point2D.Double(x, 0.0);
        if (pos.distance(event.getPosition()) > 0.001) {
            event.setPosition(pos);
        }
    }

    private void handleSignalInitialStateChange(VisualSignal signal) {
        VisualEntryEvent entry = signal.getVisualSignalEntry();
        VisualEvent nextEvent = DtdUtils.getNextVisualEvent(dtd, entry);
        if (nextEvent instanceof VisualTransitionEvent) {
            VisualTransitionEvent nextTransition = (VisualTransitionEvent) nextEvent;
            Signal.State state = signal.getInitialState();
            if (DtdUtils.getPreviousDirection(state) == nextTransition.getDirection()) {
                DtdUtils.dissolveTransitionEvent(dtd, nextTransition);
            } else if (state == Signal.State.STABLE) {
                DtdUtils.dissolveSuffixTransitionEvents(dtd, entry);
            } else {
                DtdUtils.decorateVisualLevelConnection(dtd, entry, nextEvent);
            }
        } else {
            DtdUtils.decorateVisualLevelConnection(dtd, entry, nextEvent);
        }
    }

    private void handleTransitionDirectionChange(VisualTransitionEvent transition) {
        boolean nextDone = false;
        if (transition.getDirection() == TransitionEvent.Direction.STABILISE) {
            DtdUtils.dissolvePrefixTransitionEvents(dtd, transition, TransitionEvent.Direction.DESTABILISE);
            DtdUtils.dissolveSuffixTransitionEvents(dtd, transition);
            nextDone = true;
        }
        VisualEvent nextEvent = DtdUtils.getNextVisualEvent(dtd, transition);
        if (nextEvent instanceof VisualTransitionEvent) {
            VisualTransitionEvent nextTransition = (VisualTransitionEvent) nextEvent;
            if (transition.getDirection() == nextTransition.getDirection()) {
                DtdUtils.dissolveTransitionEvent(dtd, nextTransition);
                nextDone = true;
            }
        }
        if (!nextDone) {
            DtdUtils.decorateVisualLevelConnection(dtd, transition, nextEvent);
        }

        boolean prevDone = false;
        VisualEvent prevEvent = DtdUtils.getPrevVisualEvent(dtd, transition);
        if (prevEvent instanceof VisualTransitionEvent) {
            VisualTransitionEvent prevTransition = (VisualTransitionEvent) prevEvent;
            if (prevTransition.getDirection() == transition.getDirection()) {
                DtdUtils.dissolveTransitionEvent(dtd, transition);
                prevDone = true;
            }
        }
        if (prevEvent instanceof VisualEntryEvent) {
            VisualSignal signal = transition.getVisualSignal();
            Signal.State state = signal.getInitialState();
            if (DtdUtils.getPreviousDirection(state) == transition.getDirection()) {
                DtdUtils.dissolveTransitionEvent(dtd, transition);
                prevDone = true;
            }
        }
        if (!prevDone) {
            DtdUtils.decorateVisualLevelConnection(dtd, prevEvent,  transition);
        }
    }

    private void handleSignalColorChange(VisualSignal signal) {
        Color color = signal.getForegroundColor();
        setEventAndNextLevelColor(signal.getVisualSignalEntry(), color);
        for (VisualTransitionEvent transition : signal.getVisualTransitions()) {
            setEventAndNextLevelColor(transition, color);
        }
        setEventAndNextLevelColor(signal.getVisualSignalExit(), color);
    }

    private void setEventAndNextLevelColor(VisualEvent event, Color color) {
        if (event != null) {
            event.setForegroundColor(color);
            VisualLevelConnection level = DtdUtils.getNextVisualLevel(dtd, event);
            if (level != null) {
                level.setColor(color);
            }
        }
    }

}
