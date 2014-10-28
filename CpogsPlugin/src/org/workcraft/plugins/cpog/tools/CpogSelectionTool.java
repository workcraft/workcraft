package org.workcraft.plugins.cpog.tools;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VisualArc;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualVariable;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.expressions.CpogConnector;
import org.workcraft.plugins.cpog.expressions.CpogFormula;
import org.workcraft.plugins.cpog.expressions.CpogFormulaToString;
import org.workcraft.plugins.cpog.expressions.GraphFunc;
import org.workcraft.plugins.cpog.expressions.javacc.CpogExpressionParser;
import org.workcraft.plugins.cpog.expressions.javacc.ParseException;
import org.workcraft.plugins.cpog.expressions.javacc.TokenMgrError;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.workspace.WorkspaceEntry;

import sun.misc.Queue;

public class CpogSelectionTool extends SelectionTool {

	final int margin = 4;
	final double minRadius = 2.0;
	final double expandRadius = 2.0;
	double maxX = 0, maxY = 0;
	int xpos = 0;
	boolean transitivesActive = true;

	private JTextArea expressionText;
	HashMap<String, CpogFormula> graphMap = new HashMap<String, CpogFormula>();
	final HashMap<String, Variable> variableMap = new HashMap<String, Variable>();
	private HashMap<String, String> refMap = new HashMap<String, String>();
	private Checkbox insertTransitives;

	private double highestY = 0; //Sets first graph at y co-ordinate of 0

	private CpogParsingTool parsingTool = new CpogParsingTool(variableMap, xpos, maxX, maxY, refMap);

	public CpogSelectionTool() {
		super();
	}

	public CpogSelectionTool(boolean enablePages) {
		super(enablePages);
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);
		expressionText = new JTextArea();
		expressionText.setLineWrap(false);
		expressionText.setEditable(true);
		expressionText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane expressionScroll = new JScrollPane(expressionText);

		JPanel buttonPanel = new JPanel();

		JButton btnInsert = new JButton("Insert");
		btnInsert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int prevLineEnd = 0;
				ArrayList<String> expressions = new ArrayList<String>();
				try {
					for (int i = 0; i < expressionText.getLineCount(); i++) {
						String exp = expressionText.getText().substring(prevLineEnd, expressionText.getLineEndOffset(i));

						exp = exp.replace("\n", "");
						exp = exp.replace("\t", " ");

						if (exp.compareTo("") != 0)
						{
							expressions.add(exp);
							//insertExpression(editor, exp, false);
						}

						prevLineEnd = expressionText.getLineEndOffset(i);
					}
					String exp = "";
					for (String s : expressions) {
						if (!s.contains("=")) {
							exp = exp + " " + s;
						} else {
							if (exp.compareTo("") != 0) {
								insertExpression(editor, exp, false);
								exp = "";
							}
							exp = s;
						}
					}
					if (exp.compareTo("") != 0) {
						insertExpression(editor, exp, false);
					}
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		buttonPanel.add(btnInsert);

		JButton btnOverlay = new JButton("Overlay");
		btnOverlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int prevLineEnd = 0;
				String expression = "";
				try
				{
					for (int i = 0; i < expressionText.getLineCount(); i++) {
						expression = expressionText.getText().substring(prevLineEnd, expressionText.getLineEndOffset(i));
						if (!graphMap.containsKey(expression.substring(0, expression.indexOf("=") - 1)))
						{
							insertExpression(editor, expressionText.getText().substring(prevLineEnd, expressionText.getLineEndOffset(i)), false);
						}
						prevLineEnd = expressionText.getLineEndOffset(i);
					}
				} catch (Exception e2)
				{
					e2.printStackTrace();
				}
			}
		});
		buttonPanel.add(btnOverlay);

		final JButton btnTextInsert = new JButton("Text File");
		btnTextInsert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				File textFile;
				Scanner fileIn = null;
				String equation;

				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Text Files", "txt");
				chooser.setFileFilter(filter);
				chooser.showOpenDialog(btnTextInsert);
				textFile = chooser.getSelectedFile();
				try {
					fileIn = new Scanner(textFile);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, e1.getMessage(),
							"File not found error", JOptionPane.ERROR_MESSAGE);
				}
				while (fileIn.hasNextLine()) {
					equation = fileIn.nextLine();
					insertExpression(editor, equation, true);
				}
			}

		});
		buttonPanel.add(btnTextInsert);

		final JButton btnGetGraphExpression = new JButton("Get expression");
		btnGetGraphExpression.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				parsingTool.getExpressionFromGraph(editor, expressionText);

			}

		});
		buttonPanel.add(btnGetGraphExpression);

		insertTransitives = new Checkbox("Insert Transitives", false);
		buttonPanel.add(insertTransitives);

		interfacePanel.add(expressionScroll, BorderLayout.CENTER);
		interfacePanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void insertExpression(final GraphEditor editor, String text,
			final boolean createDuplicates) {
		WorkspaceEntry we = editor.getWorkspaceEntry();
		final VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();
		we.captureMemento();

		final LinkedHashMap<String, VisualVertex> vertexMap = new LinkedHashMap<String, VisualVertex>();
		final HashSet<ArcCondition> arcConditionList = new HashSet<ArcCondition>();
		text = text.replace("\n", "");
		text = parsingTool.replaceReferences(text);


		CpogFormula f = null;
		GraphFunc<String, CpogFormula> PGF = null;
		try {
			f = CpogExpressionParser.parse(text,
					PGF = new GraphFunc<String, CpogFormula>() {

				String name;
				boolean ref;
				@Override
				public CpogFormula eval(String label) {
					if (vertexMap.containsKey(label))
						return vertexMap.get(label);

					VisualVertex vertex = null;

					// TODO: Optimise!

					if (!createDuplicates)
						for (VisualVertex v : visualCpog.getVertices(visualCpog.getCurrentLevel()))
							if (v.getLabel().equals(label)) {
								vertex = v;
								break;
							}

					if (vertex == null) {
						vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
						vertex.setLabel(label);
						vertexMap.put(label, vertex);
					}
					return vertex;
				}

				@Override
				public GraphFunc<String, CpogFormula> removeGraphName(String name){
					if (vertexMap.containsKey(name)) {
						vertexMap.remove(name);
					}
					return this;
				}

				@Override
				public CpogFormula eval(String label, String boolExpression) throws ParseException {

					VisualVertex vertex = null;
					BooleanFormula bf;

					if (vertexMap.containsKey(label))
					{
						vertex = vertexMap.get(label);
						if (boolExpression != "")
						{
							if (FormulaToString.toString(vertex.getCondition()) == "")
							{
								try {
									vertex.setCondition(parsingTool.parseBool(boolExpression, visualCpog));
								} catch (ParseException e) {
									throw new ParseException("Boolean error in: " + boolExpression);
								}
							} else
							{
								try {
									vertex.setCondition(parsingTool.parseBool(FormulaToString.toString(vertex.getCondition()) + "|" + boolExpression, visualCpog));
								} catch (ParseException e) {
									throw new ParseException("Boolean error in: " + boolExpression);
								}
							}
						}
						return vertex;
					}

					// TODO: Optimise!

					if (!createDuplicates)
						for (VisualVertex v : visualCpog.getVertices(visualCpog.getCurrentLevel()))
							if (v.getLabel().equals(label)) {
								vertex = v;
								break;
							}

					if (vertex == null) {
						vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
						vertex.setLabel(label);
						vertexMap.put(label, vertex);
					}

					if (boolExpression != "")
					{
						if (FormulaToString.toString(vertex.getCondition()) == "")
						{
							try {
								bf = parsingTool.parseBool(boolExpression, visualCpog);
								vertex.setCondition(bf);
							} catch (ParseException e) {
								throw new ParseException("Boolean error in: " + boolExpression);
							}
						} else
						{
							try {
								bf = parsingTool.parseBool(boolExpression, visualCpog);
								vertex.setCondition(bf);
							} catch (ParseException e) {
								throw new ParseException("Boolean error in: " + boolExpression);
							}
						}
					}
					return vertex;
				}

				@Override
				public String getGraphName() {
					return name;
				}

				@Override
				public void setGraphName(String graphName) {
					this.name = graphName;
					if ((name.contains("{")) && (name.contains("}"))){
						ref = true;
					}
				}

				@Override
				public void setSequenceCondition(CpogFormula formula, String boolForm) {
					arcConditionList.add(new ArcCondition(formula, boolForm));
				}

				@Override
				public boolean getRef() {
					// TODO Auto-generated method stub
					return ref;
				}

			});
		} catch (ParseException e) {
			we.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Parse error",
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (TokenMgrError e) {
			we.cancelMemento();
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Lexical error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		visualCpog.selectNone();
		int n = vertexMap.size();
		int i = 0;
		for (VisualVertex v : vertexMap.values()) {
			visualCpog.addToSelection(v);
		}

		if (!PGF.getRef()) {
			CpogConnector cc = new CpogConnector(visualCpog);
			f.accept(cc);
			graphMap.put(PGF.getGraphName(), f);

			parsingTool.setArcConditions(arcConditionList, visualCpog, vertexMap);

			HashSet<VisualVertex> roots = new HashSet<VisualVertex>();
			Set<Connection> arcs;
			Iterator<Connection> it;
			Connection connection;
			boolean second = false;
			for (Node node : visualCpog.getSelection()) {
				//VisualVertex vertex = (VisualVertex) node;
				VisualVertex v = (VisualVertex) node;
				arcs = visualCpog.getConnections(v);
				it = arcs.iterator();
				//The following covers root nodes, and nodes with no connections
				while (it.hasNext()) {
					connection = it.next();
					if (!connection.getFirst().equals(v)) {
						second = true;
						break;
					}
				}
				if (!second) {
					roots.add(v);
				}
				second = false;
			}

			if (!insertTransitives.getState()) {
				parsingTool.removeTransitives(visualCpog, roots);
			}

			if (roots.isEmpty()) {
				double y = maxY + 2.5;
				for (VisualVertex v : vertexMap.values()) {
					double radius = Math.max(minRadius, expandRadius * n / Math.PI
							/ 2.0);
					Point2D.Double pos = new Point2D.Double(maxX + radius
							* Math.cos(2.0 * Math.PI * i / n), y + radius * Math.sin(2.0 * Math.PI * i / n));
					v.setPosition(pos);
					if (pos.y > highestY) {
						highestY = pos.y;
					}
					i++;
				}
			} else {
				Iterator<VisualVertex> root = roots.iterator();
				Queue q = new Queue();
				//double originalX = maxX;
				while(root.hasNext()) {
					q.enqueue(root.next());
					parsingTool.bfsLayout(q, visualCpog, 0);
				}
			}

			maxY += 2;
			editor.requestFocus();
			if (PGF.getGraphName() != null)	{
				visualCpog.groupSelection(PGF.getGraphName());
			}
		} else {
			int index = text.indexOf("= ");
			text = text.substring(index + 2);
			parsingTool.addToReferenceList(PGF.getGraphName(), visualCpog, text);
			visualCpog.remove(visualCpog.getSelection());
		}

		editor.forceRedraw();

		// TODO: fix the bug after exception; find out if the line below is
		// needed
		// I think it is fixed now (by not keeping a reference to the
		// visualModel in the activated method)
		we.saveMemento();
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		boolean processed = false;

		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			VisualModel model = e.getEditor().getModel();
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(
					e.getPosition(), model);
			if (node != null) {
				if (node instanceof VisualVariable) {
					VisualVariable var = (VisualVariable) node;
					var.toggle();
					processed = true;
				}
			}
		}

		if (!processed) {
			super.mouseClicked(e);

		}
	}
}


