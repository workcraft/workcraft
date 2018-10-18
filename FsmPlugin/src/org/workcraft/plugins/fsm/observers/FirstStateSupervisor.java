package org.workcraft.plugins.fsm.observers;

import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddingEvent;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.util.Hierarchy;

import java.util.Collection;

public class FirstStateSupervisor extends HierarchySupervisor {

    @Override
    public void handleEvent(HierarchyEvent e) {
        // Make the first created state initial
        if (e instanceof NodesAddingEvent) {
            Collection<VisualState> existingStates = Hierarchy.getChildrenOfType(getRoot(), VisualState.class);
            if (existingStates.isEmpty()) {
                Collection<VisualState> newStates = Hierarchy.filterNodesByType(e.getAffectedNodes(), VisualState.class);
                if (!newStates.isEmpty()) {
                    VisualState state = newStates.iterator().next();
                    state.getReferencedState().setInitial(true);
                }
            }
        }
    }

}
