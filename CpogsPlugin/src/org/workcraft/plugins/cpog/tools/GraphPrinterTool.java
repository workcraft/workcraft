package org.workcraft.plugins.cpog.tools;

import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Container;
import org.workcraft.plugins.cpog.CPOG;
import org.workcraft.plugins.cpog.VisualArc;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics.PrinterSuite;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphPrinterTool implements Tool {
	private final Framework framework;

	public GraphPrinterTool(Framework framework) {
		this.framework = framework;
	}

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
		return we.getModelEntry().getMathModel() instanceof CPOG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualCPOG cpog = (VisualCPOG)we.getModelEntry().getVisualModel();
		System.out.println("=================");
		System.out.println("Graph statistics:");
		System.out.println("=================");
		Container root = cpog.getRoot();

		HashSet<String> F = new HashSet<String>();

		int simpleVertices = 0, simpleArcs = 0;

		for(VisualVertex v : cpog.getVertices(root))
		{
			if (v.getCondition() == One.instance() || v.getCondition() == Zero.instance()) {
				simpleVertices++;
			} else {
				F.add(FormulaToString.toString(v.getCondition()));
			}
		}

		for(VisualArc a : cpog.getArcs(root))
		{
			if (a.getCondition() == One.instance() || a.getCondition() == Zero.instance()) {
				simpleArcs++;
			} else {
				F.add(FormulaToString.toString(a.getCondition()));
			}
		}

		System.out.println("Number of vertices: " + cpog.getVertices(root).size() + " (" + simpleVertices + " unconditional)");
		System.out.println("Number of arcs: " + cpog.getArcs(root).size() + " (" + simpleArcs + " unconditional)");
		System.out.println("Number of variables: " + cpog.getVariables(root).size());
		System.out.println("Number of conditions: " + F.size());
		for(String s : F) {
			System.out.println("  " + s);
		}
	}

}
