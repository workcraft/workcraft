package org.workcraft.plugins.wtg.supervisors;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.wtg.State;
import org.workcraft.util.Hierarchy;

public class InitialStateSupervisor extends StateSupervisor {

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            if ((sender instanceof State) && pce.getPropertyName().equals(State.PROPERTY_INITIAL)) {
                // Update all the states on a change of the initial property
                State state = (State) sender;
                if (state.isInitial()) {
                    for (State s: Hierarchy.getChildrenOfType(state.getParent(), State.class)) {
                        if (s == state) continue;
                        s.setInitialQuiet(false);
                    }
                }
            }
        }
    }

    @Override
    public void handleHierarchyEvent(HierarchyEvent e) {
        if (e instanceof NodesAddingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof State) {
                    // Make pasted states non-initial
                    ((State) node).setInitialQuiet(false);
                }
            }
        }
    }

}
