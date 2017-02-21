package org.workcraft.plugins.dtd;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;

public class SignalConsisitencySupervisor extends HierarchySupervisor {
    private final VisualDtd dtd;

    public SignalConsisitencySupervisor(VisualDtd dtd) {
        this.dtd = dtd;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof VisualSignal) {
                    final VisualSignal signal = (VisualSignal) node;
                    handleSignalRemoval(signal);
                }
            }
        }
    }

    private void handleSignalRemoval(final VisualSignal signal) {
        Collection<VisualTransition> transitions = dtd.getVisualTransitions(signal);
        for (VisualTransition transition: transitions) {
            dtd.remove(transition);
        }
    }

}
