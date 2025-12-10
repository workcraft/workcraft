package org.workcraft.plugins.fsm.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.State;
import org.workcraft.utils.Hierarchy;

public class InitialStateSupervisor extends StateSupervisor {

    private final Fsm fsm;

    public InitialStateSupervisor(Fsm fsm) {
        this.fsm = fsm;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent pce) {
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
            for (Node node : e.getAffectedNodes()) {
                if (node instanceof State) {
                    // Move the initial property to another state on state removal
                    handleStateRemoval((State) node);
                }
            }
        } else if (e instanceof NodesAddingEvent) {
            for (Node node : e.getAffectedNodes()) {
                if (node instanceof State) {
                    // Make pasted states non-initial
                    ((State) node).setInitialQuiet(false);
                }
            }
        }
    }

    private void handleInitialStateChange(State state) {
        for (State otherState : Hierarchy.getChildrenOfType(state.getParent(), State.class)) {
            if (otherState == state) continue;
            if (state.isInitial()) {
                otherState.setInitialQuiet(false);
            } else {
                otherState.setInitialQuiet(true);
                break;
            }
        }
    }

    private void handleStateRemoval(State state) {
        if (state.isInitial()) {
            for (State otherState : fsm.getStates()) {
                if (otherState != state) {
                    otherState.setInitial(true);
                    break;
                }
            }
        }
    }

}
