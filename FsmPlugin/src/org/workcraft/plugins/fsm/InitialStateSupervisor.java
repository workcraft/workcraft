package org.workcraft.plugins.fsm;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.util.Hierarchy;

public class InitialStateSupervisor extends StateSupervisor {
    private final Fsm fsm;

    public InitialStateSupervisor(Fsm fsm) {
        this.fsm = fsm;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            if ((sender instanceof State) && pce.getPropertyName().equals(State.PROPERTY_INITIAL)) {
                // Update all the states on a change of the initial property
                handleInitialStateChange((State) sender);
            }
        }
    }

    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof State) {
                    // Move the initial property to another state on state removal
                    handleStateRemoval((State) node);
                }
            }
        } else if (e instanceof NodesAddingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof State) {
                    // Make pasted states non-initial
                    ((State) node).setInitialQuiet(false);
                }
            }
        }
    }

    private void handleInitialStateChange(State state) {
        for (State s: Hierarchy.getChildrenOfType(state.getParent(), State.class)) {
            if (!s.equals(state)) {
                if (state.isInitial()) {
                    s.setInitialQuiet(false);
                } else {
                    s.setInitialQuiet(true);
                    break;
                }
            }
        }
    }

    private void handleStateRemoval(State state) {
        if (state.isInitial()) {
            for (State s: fsm.getStates()) {
                if (!s.equals(state)) {
                    s.setInitial(true);
                    break;
                }
            }
        }
    }

}
