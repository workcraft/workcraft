package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.AbstractStatisticsCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.cpog.*;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CpogStatisticsCommand extends AbstractStatisticsCommand {

    @Override
    public String getDisplayName() {
        return "Scenario analysis (selected or current)";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public String getStatistics(WorkspaceEntry we) {
        VisualCpog cpog = WorkspaceUtils.getAs(we, VisualCpog.class);

        Set<Container> scenarios = new HashSet<>();
        for (Node node: cpog.getSelection()) {
            if (node instanceof VisualScenario) {
                scenarios.add((VisualScenario) node);
            }
            if (node instanceof VisualScenarioPage) {
                scenarios.add((VisualScenarioPage) node);
            }
        }

        boolean useCurrentLevel = scenarios.isEmpty();
        if (useCurrentLevel) {
            scenarios.add(cpog.getCurrentLevel());
            scenarios.add(cpog.getRoot());
        }

        int variableCount = 0;
        int vertexCount = 0;
        int simpleVertexCount = 0;
        int arcCount = 0;
        int simpleArcCount = 0;
        HashSet<String> conditions = new HashSet<>();
        for (Container scenario : scenarios) {
            variableCount += cpog.getVariables(scenario).size();

            Collection<VisualVertex> vertices = cpog.getVertices(scenario);
            vertexCount += vertices.size();
            for (VisualVertex vertex : vertices) {
                if ((vertex.getCondition() == One.getInstance()) || (vertex.getCondition() == Zero.getInstance())) {
                    simpleVertexCount++;
                } else {
                    conditions.add(StringGenerator.toString(vertex.getCondition()));
                }
            }

            arcCount += cpog.getArcs(scenario).size();
            for (VisualArc arc : cpog.getArcs(scenario)) {
                if ((arc.getCondition() == One.getInstance()) || (arc.getCondition() == Zero.getInstance())) {
                    simpleArcCount++;
                } else {
                    conditions.add(StringGenerator.toString(arc.getCondition()));
                }
            }
        }

        return (useCurrentLevel ? "Statistics for current scenario:" : "Statistics for selected scenarios:")
                + "\n  Vertex count -  " + vertexCount + " (" + simpleVertexCount + " unconditional)"
                + "\n  Arc count -  " + arcCount + " (" + simpleArcCount + " unconditional)"
                + "\n  Variable count -  " + variableCount + getConditionsCount(conditions);
    }

    private String getConditionsCount(HashSet<String> conditions) {
        if (conditions.isEmpty()) {
            return "\n";
        }
        return "\n  Conditions (" + conditions.size() + " in total) - " + String.join(", ", conditions) + '\n';
    }

}
