package org.workcraft.plugins.cpog.observers;

import org.workcraft.dom.Node;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.cpog.Arc;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.Vertex;

import java.util.ArrayList;

public final class ConditionConsistencySupervisor extends HierarchySupervisor {

    private static final BooleanWorker WORKER = new CleverBooleanWorker();

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
        for (Vertex vertex : new ArrayList<>(cpog.getVertices())) {
            vertex.setCondition(FormulaUtils.replaceZero(vertex.getCondition(), var, WORKER));
        }
        for (Arc arc : new ArrayList<>(cpog.getArcs())) {
            arc.setCondition(FormulaUtils.replaceZero(arc.getCondition(), var, WORKER));
        }
    }

}
