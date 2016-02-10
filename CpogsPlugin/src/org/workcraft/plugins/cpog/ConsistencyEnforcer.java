package org.workcraft.plugins.cpog;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesAddedEvent;
import org.workcraft.observation.NodesDeletedEvent;
import org.workcraft.observation.NodesReparentedEvent;

public class ConsistencyEnforcer extends HierarchySupervisor {

    private final VisualCPOG visualCPOG;
    private int vertexCount = 0;
    private int variableCount = 0;

    public ConsistencyEnforcer(VisualCPOG visualCPOG) {
        this.visualCPOG  = visualCPOG;
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
        for(Node node : e.getAffectedNodes()) {
            if (node instanceof VisualVertex) {
                VisualVertex vertex = (VisualVertex) node;
                if (vertex.getLabel().isEmpty())
                vertex.setLabel("v_" + vertexCount++);
            }
            if (node instanceof VisualVariable) {
                VisualVariable variable = (VisualVariable) node;
                if (variable.getLabel().isEmpty()) {
                    variable.setLabel("x_" + variableCount++);
                }
            }
        }
    }

    private void updateEncoding() {
        for(VisualScenario group : visualCPOG.getGroups()) {
            Encoding oldEncoding = group.getEncoding();
            Encoding newEncoding = new Encoding();

            for(VisualVariable var : visualCPOG.getVariables()) {
                Variable mathVariable = var.getMathVariable();
                newEncoding.setState(mathVariable, oldEncoding.getState(mathVariable));
            }

            group.setEncoding(newEncoding);
        }

    }

}
