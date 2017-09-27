package org.workcraft.plugins.cpog.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.commands.Command;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.plugins.cpog.VisualArc;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class GraphStatisticsCommand implements Command {

    @Override
    public String getDisplayName() {
        return "Print graph statistics";
    }

    @Override
    public String getSection() {
        return "Information";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualCpog cpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        ArrayList<Container> scenarios = new ArrayList<>();
        for (Node cur: cpog.getSelection()) {
            if (cur instanceof VisualScenario) {
                scenarios.add((VisualScenario) cur);
            }
        }
        if (scenarios.isEmpty()) {
            scenarios.add(cpog.getCurrentLevel());
            printHeaderCurrent();
        } else {
            printHeaderSelected();
        }
        printStatistics(cpog, scenarios);
    }

    private void printHeaderCurrent() {
        System.out.println("================================");
        System.out.println("Statistics for current scenario:");
        System.out.println("================================");
    }

    private void printHeaderSelected() {
        System.out.println("==================================");
        System.out.println("Statistics for selected scenarios:");
        System.out.println("==================================");
    }

    private void printStatistics(VisualCpog cpog, Collection<Container> scenarios) {
        HashSet<String> conditions = new HashSet<>();
        int allVertices = 0, simpleVertices = 0;
        int allArcs = 0, simpleArcs = 0;
        int allVariables = 0;
        for (Container scenario: scenarios) {
            allVertices += cpog.getVertices(scenario).size();
            for (VisualVertex v : cpog.getVertices(scenario)) {
                if (v.getCondition() == One.instance() || v.getCondition() == Zero.instance()) {
                    simpleVertices++;
                } else {
                    conditions.add(FormulaToString.toString(v.getCondition()));
                }
            }

            allArcs += cpog.getArcs(scenario).size();
            for (VisualArc a : cpog.getArcs(scenario)) {
                if (a.getCondition() == One.instance() || a.getCondition() == Zero.instance()) {
                    simpleArcs++;
                } else {
                    conditions.add(FormulaToString.toString(a.getCondition()));
                }
            }

            allVariables += cpog.getVariables(scenario).size();
        }

        System.out.println("Number of vertices: " + allVertices + " (" + simpleVertices + " unconditional)");
        System.out.println("Number of arcs: " + allArcs + " (" + simpleArcs + " unconditional)");
        System.out.println("Number of variables: " + allVariables);
        System.out.println("Number of conditions: " + conditions.size());
        for (String condition : conditions) {
            System.out.println("  " + condition);
        }
    }

}
