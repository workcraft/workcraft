package org.workcraft.plugins.cpog;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;

import org.workcraft.Tool;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.CleverCnfGenerator;
import org.workcraft.plugins.cpog.optimisation.CpogEncoding;
import org.workcraft.plugins.cpog.optimisation.DefaultCpogSolver;
import org.workcraft.plugins.cpog.optimisation.OneHotIntBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.OneHotNumberProvider;
import org.workcraft.plugins.cpog.optimisation.Optimiser;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.util.Geometry;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogEncoder implements Tool {

	@Override
	public String getDisplayName() {
		return "CPOG Encoding";
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


	private String generateConstraint(char [][][] constraints, int numScenarios, int event1, int event2)
	{
		StringBuilder s = new StringBuilder();
		for(int k = 0; k < numScenarios; k++) s.append(constraints[k][event1][event2]);
		return s.toString();
	}

	private char trivialEncoding(char [][][] constraints, int numScenarios, int event1, int event2)
	{
		char trivial = '-';

		for(int k = 0; k < numScenarios; k++)
		{
			if (constraints[k][event1][event2] == '0')
			{
				if (trivial == '1') return '?';
				trivial = '0';
			}

			if (constraints[k][event1][event2] == '1')
			{
				if (trivial == '0') return '?';
				trivial = '1';
			}
		}

		return trivial;
	}

	@Override
	public void run(WorkspaceEntry we)
	{
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());

		we.captureMemento();

		HashMap<String, Integer> events = new HashMap<String, Integer>();
		int n = 0;
		ArrayList<Point2D> positions = new ArrayList<Point2D>();
		ArrayList<Integer> count = new ArrayList<Integer>();

		ArrayList<VisualScenario> scenarios = new ArrayList<VisualScenario>(cpog.getGroups());
		int m = scenarios.size();

		// find all events
		for(int k = 0; k < m; k++)
		{
			for(VisualComponent component : scenarios.get(k).getComponents())
			if (component instanceof VisualVertex)
			{
				VisualVertex vertex = (VisualVertex)component;

				if (!events.containsKey(vertex.getLabel()))
				{
					events.put(vertex.getLabel(), n);
					count.add(1);
					Point2D p = vertex.getCenter();
					p.setLocation(p.getX() - scenarios.get(k).getBoundingBox().getMinX(), p.getY() - scenarios.get(k).getBoundingBox().getMinY());
					positions.add(p);
					n++;
				}
				else
				{
					int id = events.get(vertex.getLabel());
					count.set(id, count.get(id) + 1);
					Point2D p = vertex.getCenter();
					p.setLocation(p.getX() - scenarios.get(k).getBoundingBox().getMinX(), p.getY() - scenarios.get(k).getBoundingBox().getMinY());
					positions.set(id, Geometry.add(positions.get(id), p));
				}
			}
		}

		// construct constraints

		char [][][] constraints = new char[m][n][n];
		int [][] graph = new int[n][n];

		for(int k = 0; k < m; k++)
		{
			for(int i = 0; i < n; i++) for(int j = 0; j < n; j++) constraints[k][i][j] = '0';

			for(VisualComponent component : scenarios.get(k).getComponents())
			if (component instanceof VisualVertex)
			{
				VisualVertex vertex = (VisualVertex)component;
				int id = events.get(vertex.getLabel());
				constraints[k][id][id] = '1';
			}

			for(int i = 0; i < n; i++) for(int j = 0; j < n; j++) graph[i][j] = 0;

			for(VisualConnection c : scenarios.get(k).getConnections())
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
												"Scenario '" + scenarios.get(k).getLabel() + "' is cyclic.",
												"Invalid scenario",
												JOptionPane.ERROR_MESSAGE);
					we.cancelMemento();
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
		}

		// group similar constraints

		HashMap<String, Integer> task = new HashMap<String, Integer>();

		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				if (trivialEncoding(constraints, m, i, j) == '?')
				{
					String constraint = generateConstraint(constraints, m, i, j);
					if (!task.containsKey(constraint)) task.put(constraint, task.size());
				}

		// call CPOG encoder

		char [][] matrix = new char[m][task.size()];

		String [] instance = new String[m];
		for(String s : task.keySet())
			for(int i = 0; i < m; i++) matrix[i][task.get(s)] = s.charAt(i);

		for(int i = 0; i < m; i++) instance[i] = new String(matrix[i]);

		int freeVariables = CpogSettings.getEncodingWidth();
		int derivedVariables = CpogSettings.getCircuitSize();

		Optimiser<OneHotIntBooleanFormula> oneHot = new Optimiser<OneHotIntBooleanFormula>(new OneHotNumberProvider());

		DefaultCpogSolver<BooleanFormula> solverCnf = new DefaultCpogSolver<BooleanFormula>(oneHot, new CleverCnfGenerator());

		Variable [] vars = new Variable[freeVariables];
		for(int i = 0; i < freeVariables; i++) vars[i] = cpog.createVisualVariable().getMathVariable();

		CpogEncoding solution = null;
		try
		{
			solution = solverCnf.solve(instance, vars, derivedVariables);
			if (solution == null)
			{
				we.cancelMemento();
				JOptionPane.showMessageDialog(null, "No solution.", "Encoding result", JOptionPane.INFORMATION_MESSAGE);
			}

		}
		catch(Exception e)
		{
			we.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Encoding result", JOptionPane.ERROR_MESSAGE);
		}

		if(solution == null) return;

		// create result

		boolean[][] encoding = solution.getEncoding();

		for(int k = 0; k < m; k++)
		{
			for(int i = 0; i < freeVariables; i++)
				scenarios.get(k).getEncoding().setState(vars[i], VariableState.fromBoolean(encoding[k][i]));
		}

		VisualScenario result = cpog.createVisualScenario();
		result.setLabel("Composition");
		VisualVertex [] vertices = new VisualVertex[n];
		for(String eventName : events.keySet())
		{
			int id = events.get(eventName);
			vertices[id] = cpog.createVisualVertex(result);
			vertices[id].setLabel(eventName);
			vertices[id].setPosition(Geometry.multiply(positions.get(id), 1.0/count.get(id)));
		}

		BooleanFormula[] functions = solution.getFunctions();
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
			{
				BooleanFormula condition;

				char trivial = trivialEncoding(constraints, m, i, j);
				if (trivial != '?')
				{
					if (trivial == '1')
					{
						condition = One.instance();
					}
					else
					{
						continue;
					}
				}
				else
				{
					String constraint = generateConstraint(constraints, m, i, j);
					condition = functions[task.get(constraint)];
				}

				if (i == j)
				{
					vertices[i].setCondition(condition);
				}
				else
				{
					VisualArc arc = cpog.connect(vertices[i], vertices[j]);
					arc.setCondition(condition);
				}
			}
		we.saveMemento();
	}

}
