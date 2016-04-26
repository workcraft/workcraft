package org.workcraft.plugins.dtd;

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
        if ((signal != null) && (signal.getY() != transition.getY())) {
            transition.setY(signal.getY());
        }
    }

    private void handleSignalTransformation(VisualSignal signal) {
        for (VisualTransition transition: dtd.getVisualTransitions(signal)) {
            if (transition.getY() != signal.getY()) {
                transition.setY(signal.getY());
            }
        }
    }

    private void handleComponentTransformation(VisualComponent component) {
        VisualComponent minComponent = null;
        for (Node predNode: dtd.getPreset(component)) {
            VisualComponent predComponent = (VisualComponent) predNode;
            if ((minComponent == null) || (minComponent.getX() < predComponent.getX())) {
                minComponent = predComponent;
            }
        }
        VisualComponent maxComponent = null;
        for (Node succNode: dtd.getPostset(component)) {
            VisualComponent succComponent = (VisualComponent) succNode;
            if ((maxComponent == null) || (maxComponent.getX() > succComponent.getX())) {
                maxComponent = succComponent;
            }
        }
        if ((minComponent != null) && (component.getX() < minComponent.getX())) {
            component.setX(minComponent.getX());
        }
        if ((maxComponent != null) && (component.getX() > maxComponent.getX())) {
            component.setX(maxComponent.getX());
        }
    }

}
