package org.workcraft.plugins.cpog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CnfGeneratingOptimiser;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.CpogSolver;
import org.workcraft.plugins.cpog.optimisation.DefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.LimBooleCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.MiniSatBooleanSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogEncoder implements Tool {

	@Override
	public String getDisplayName() {
		return "CPOG Encoding...";
	}

	@Override
	public String getSection() {
		return "Encoding";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we)
	{
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	@Override
	public void run(WorkspaceEntry we)
	{
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());

		HashMap<String, Integer> events = new HashMap<String, Integer>();
		int n = 0;
		int m = cpog.getGroups().size();

		// find all events
		for(VisualScenario scenario : cpog.getGroups())
		{
			for(VisualComponent component : scenario.getComponents())
			if (component instanceof VisualVertex)
			{
				VisualVertex vertex = (VisualVertex)component;

				if (!events.containsKey(vertex.getLabel()))
				{
					events.put(vertex.getLabel(), n);
					System.out.println("Event added: " + vertex.getLabel());
					n++;
				}
			}
		}

		// construct constraints

		char [][][] constraints = new char[m][n][n];
		int [][] graph = new int[n][n];

		int k = 0;
		for(VisualScenario scenario : cpog.getGroups())
		{
			for(int i = 0; i < n; i++) for(int j = 0; j < n; j++) constraints[k][i][j] = '0';

			for(VisualComponent component : scenario.getComponents())
			if (component instanceof VisualVertex)
			{
				VisualVertex vertex = (VisualVertex)component;
				int id = events.get(vertex.getLabel());
				constraints[k][id][id] = '1';
			}

			for(int i = 0; i < n; i++) for(int j = 0; j < n; j++) graph[i][j] = 0;

			for(VisualConnection c : scenario.getConnections())
			if (c instanceof VisualArc)
			{
				VisualArc arc = (VisualArc)c;
				VisualComponent c1 = arc.getFirst(), c2 = arc.getSecond();
				if (c1 instanceof VisualVertex && c2 instanceof VisualVertex)
				{
					int id1 = events.get(((VisualVertex)c1).getLabel());
					int id2 = events.get(((VisualVertex)c2).getLabel());
					graph[id1][id2] = 1;
				}
			}

			// compute transitive closure

			for(int t = 0; t < n; t++)
				for(int i = 0; i < n; i++)
				if (graph[i][t] > 0)
					for(int j = 0; j < n; j++)
					if (graph[t][j] > 0) graph[i][j] = 1;

			// detect transitive arcs

			for(int t = 0; t < n; t++)
				for(int i = 0; i < n; i++)
				if (graph[i][t] > 0)
					for(int j = 0; j < n; j++)
					if (graph[t][j] > 0) graph[i][j] = 2;

			// report cyclic scenario

			for(int i = 0; i < n; i++)
				if (graph[i][i] > 0)
				{
					JOptionPane.showMessageDialog(null,
												"Scenario " + scenario.getLabel() + " is cyclic.",
												"Invalid scenario",
												JOptionPane.ERROR_MESSAGE);
					return;
				}

			for(int i = 0; i < n; i++)
				for(int j = 0; j < n; j++)
				if (i != j)
				{
					char ch = '0';

					if (graph[i][j] > 0) ch = '1';
					if (graph[i][j] > 1) ch = '-';
					if (constraints[k][i][i] == '0' || constraints[k][j][j] == '0') ch = '-';

					constraints[k][i][j] = ch;
				}

			k++;
		}

		// group similar constraints

		HashSet<String> task = new HashSet<String>();

		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
			{
				StringBuilder s = new StringBuilder();
				char trivial = '-';
				for(int sc = 0; sc < n; sc++)
				{
					s.append(constraints[sc][i][j]);
					if (trivial == '?') continue;

					if (constraints[sc][i][j] == '0')
					{
						if (trivial == '1')
							trivial = '?';
						else
							trivial = '0';
					}

					if (constraints[sc][i][j] == '1')
					{
						if (trivial == '0')
							trivial = '?';
						else
							trivial = '1';
					}
				}

				if (trivial == '?') task.add(s.toString());
			}

		// call CPOG encoder

		String [] instance = new String[task.size()];

		System.out.println("Set of non-trivial contraints:");
		k = 0;
		for(String s : task)
		{
			instance[k] = s;
			System.out.println(s);
			k++;
		}

		int freeVariables = 1;
		int derivedVariables = 3;

		Optimiser<OneHotIntBooleanFormula> oneHot = new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider());

		CpogSolver solverCnf = new DefaultCpogSolver<BooleanFormula>(oneHot, new CleverCnfGenerator());

		CpogEncoding solution = solverCnf.solve(instance, freeVariables, derivedVariables);

		if(solution == null)
			System.out.println("No solution.");
		else
		{
			boolean[][] encoding = solution.getEncoding();
			for(int i=0;i<encoding.length;i++)
			{
				for(int j=0;j<encoding[i].length;j++)
					System.out.print(encoding[i][j]?1:0);
				System.out.println();
			}

			System.out.println("Functions:");
			BooleanFormula[] functions = solution.getFunctions();
			for(int i=0;i<functions.length;i++)
			{
				System.out.println(FormulaToString.toString(functions[i]));
			}
		}

	}

}
