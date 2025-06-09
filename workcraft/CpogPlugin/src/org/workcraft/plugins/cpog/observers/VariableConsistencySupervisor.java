package org.workcraft.plugins.cpog.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.*;
import org.workcraft.plugins.cpog.*;

public class VariableConsistencySupervisor extends HierarchySupervisor {

    private final VisualCpog cpog;
    private int vertexCount = 0;
    private int variableCount = 0;

    public VariableConsistencySupervisor(VisualCpog cpog) {
        this.cpog = cpog;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if ((e instanceof NodesAddedEvent) || (e instanceof NodesReparentedEvent)) {
            createDefaultLabels(e);
            updateEncoding();
        } else if (e instanceof NodesDeletedEvent) {
            updateEncoding();
        }
    }

    private void createDefaultLabels(HierarchyEvent e) {
        for (Node node : e.getAffectedNodes()) {
            if (node instanceof VisualVertex vertex) {
                if (vertex.getLabel().isEmpty()) {
                    vertex.setLabel("v_" + vertexCount++);
                }
            }
            if (node instanceof VisualVariable variable) {
                if (variable.getLabel().isEmpty()) {
                    variable.setLabel("x_" + variableCount++);
                }
            }
        }
    }

    private void updateEncoding() {
        for (VisualScenario scenario : cpog.getScenarios()) {
            Encoding oldEncoding = scenario.getEncoding();
            Encoding newEncoding = new Encoding();
            for (VisualVariable var : cpog.getVariables()) {
                Variable mathVariable = var.getReferencedComponent();
                newEncoding.setState(mathVariable, oldEncoding.getState(mathVariable));
            }
            scenario.setEncoding(newEncoding);
        }
    }

}
