package org.workcraft.plugins.cpog;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public final class ConditionConsistencySupervisor extends HierarchySupervisor {

    private final CPOG cpog;

    public ConditionConsistencySupervisor(CPOG cpog) {
        this.cpog = cpog;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        // Update all vertex conditions when a variable is removed
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof Variable) {
                    final Variable var = (Variable)node;
                    removeVariableFromConditions(var);
                }
            }
        }
    }

    private void removeVariableFromConditions(final Variable var) {
        for (Vertex v: new ArrayList<Vertex>(cpog.getVertices())) {
            v.setCondition(BooleanUtils.cleverReplace(v.getCondition(), var, Zero.instance()));
        }
    }

}
