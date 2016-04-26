package org.workcraft.plugins.dtd;

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
                handleTransitionTransformation((VisualTransition) tce.getSender());
            } else if (tce.getSender() instanceof VisualSignal) {
                handleSignalTransformation((VisualSignal) tce.getSender());
            }
        }
    }

    private void handleTransitionTransformation(VisualTransition transition) {
        VisualSignal signal = dtd.getVisualSignal(transition);
        if (signal != null) {
            if (signal.getY() != transition.getY()) {
                transition.setY(signal.getY());
            }
        }
    }

    private void handleSignalTransformation(VisualSignal signal) {
        double xMin = signal.getX();
        boolean first = true;
        for (VisualTransition transition: dtd.getVisualTransitions(signal)) {
            if (transition.getY() != signal.getY()) {
                transition.setY(signal.getY());
            }
            if (first || (transition.getX() < xMin)) {
                xMin = transition.getX() - 1.0;
                first = false;
            }
        }
        if (signal.getX() > xMin) {
            signal.setX(xMin);
        }
    }

}
