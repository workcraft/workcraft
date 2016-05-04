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
        double y = signal.getRootSpaceY();
        if ((signal != null) && (y != transition.getRootSpaceY())) {
            transition.setRootSpaceY(y);
        }
    }

    private void handleSignalTransformation(VisualSignal signal) {
        for (VisualTransition transition: dtd.getVisualTransitions(signal)) {
            double y = signal.getRootSpaceY();
            if (transition.getRootSpaceY() != y) {
                transition.setRootSpaceY(y);
            }
        }
    }

    private void handleComponentTransformation(VisualComponent component) {
        VisualComponent minComponent = null;
        for (Node predNode: dtd.getPreset(component)) {
            VisualComponent predComponent = (VisualComponent) predNode;
            if ((minComponent == null) || (minComponent.getRootSpaceX() < predComponent.getRootSpaceX())) {
                minComponent = predComponent;
            }
        }
        VisualComponent maxComponent = null;
        for (Node succNode: dtd.getPostset(component)) {
            VisualComponent succComponent = (VisualComponent) succNode;
            if ((maxComponent == null) || (maxComponent.getRootSpaceX() > succComponent.getRootSpaceX())) {
                maxComponent = succComponent;
            }
        }
        if ((minComponent != null) && (component.getRootSpaceX() < minComponent.getRootSpaceX())) {
            component.setRootSpaceX(minComponent.getRootSpaceX());
        }
        if ((maxComponent != null) && (component.getRootSpaceX() > maxComponent.getRootSpaceX())) {
            component.setRootSpaceX(maxComponent.getRootSpaceX());
        }
    }

}
