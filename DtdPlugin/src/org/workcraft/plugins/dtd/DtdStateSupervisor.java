package org.workcraft.plugins.dtd;

import java.awt.geom.Rectangle2D;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
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
        }
    }

    private void handleTransitionTransformation(VisualTransition transition) {
        VisualSignal signal = dtd.getVisualSignal(transition);
        double y = signal.getY();
        if ((signal != null) && (y != transition.getY())) {
            transition.setY(y);
        }
    }

    private void handleSignalTransformation(VisualSignal signal) {
        for (VisualTransition transition: dtd.getVisualTransitions(signal)) {
            double y = signal.getY();
            if (transition.getY() != y) {
                transition.setY(y);
            }
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
            double xOffset =  component.getX() - bb.getMinX();
            component.setX(xMin + xOffset);
        } else if (xMax < bb.getMaxX()) {
            double xOffset = bb.getMaxX() - component.getX();
            component.setX(xMax - xOffset);
        }
    }

}
