package org.workcraft.plugins.cpog;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;

public final class ConditionConsistencySupervisor extends HierarchySupervisor {

    private final Cpog cpog;

    public ConditionConsistencySupervisor(Cpog cpog) {
        this.cpog = cpog;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        // Update all vertex conditions when a variable is removed
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof Variable) {
                    final Variable var = (Variable) node;
                    removeVariableFromConditions(var);
                }
            }
        }
    }

    private void removeVariableFromConditions(final Variable var) {
        for (Vertex v: new ArrayList<>(cpog.getVertices())) {
            v.setCondition(BooleanUtils.replaceClever(v.getCondition(), var, Zero.instance()));
        }
        for (Arc a: new ArrayList<>(cpog.getArcs())) {
            a.setCondition(BooleanUtils.replaceClever(a.getCondition(), var, Zero.instance()));
        }
    }

}
