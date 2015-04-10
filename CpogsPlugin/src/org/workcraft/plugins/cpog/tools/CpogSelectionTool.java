package org.workcraft.plugins.cpog.tools;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultHangingConnectionRemover;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.observation.*;
import org.workcraft.plugins.cpog.*;
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

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;



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
	private HashMap<String, HashMap<String, VisualVertex>> refVertMap = new HashMap<String, HashMap<String, VisualVertex>>();
	private Checkbox insertTransitives;

	private double highestY = 0; //Sets first graph at y co-ordinate of 0

	private CpogParsingTool parsingTool = new CpogParsingTool(variableMap, xpos, maxX, maxY, refMap, refVertMap);

	private ArrayList<VisualPage> refPages = new ArrayList<VisualPage>();

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
						}

						prevLineEnd = expressionText.getLineEndOffset(i);
					}
					String exp = "";
					for (String s : expressions) {
						if (!s.contains("=")) {
							exp = exp + " " + s;
						} else {
							if (exp.compareTo("") != 0) {
								insertExpression(editor, exp, false, false);
								exp = "";
							}
							exp = s;
						}
					}
					if (exp.compareTo("") != 0) {
						insertExpression(editor, exp, false, false);
					}
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		buttonPanel.add(btnInsert);

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
					insertExpression(editor, equation, true, false);
				}
			}

		});
		buttonPanel.add(btnTextInsert);

		final JButton btnGetGraphExpression = new JButton("Get expression");
		btnGetGraphExpression.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				expressionText.setText(parsingTool.getExpressionFromGraph((VisualCPOG) editor.getWorkspaceEntry().getModelEntry().getVisualModel()));
			}

		});
		buttonPanel.add(btnGetGraphExpression);

		insertTransitives = new Checkbox("Insert Transitives", false);
		controlPanel.add(insertTransitives);

		interfacePanel.add(expressionScroll, BorderLayout.CENTER);
		interfacePanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private HashMap<String, VisualVertex> insertExpression(final GraphEditor editor, String text,
			final boolean createDuplicates, boolean getVertList) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        final VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();
        we.captureMemento();

        if ((visualCpog.getCurrentLevel() instanceof VisualPage) && (!getVertList)) {
            while (visualCpog.getCurrentLevel() instanceof VisualPage) {
                Container c = (Container) visualCpog.getCurrentLevel().getParent();
                visualCpog.setCurrentLevel(c);
            }
        }

        final LinkedHashMap<String, VisualVertex> vertexMap = new LinkedHashMap<String, VisualVertex>();
        final HashSet<ArcCondition> arcConditionList = new HashSet<ArcCondition>();
        text = text.replace("\n", "");
        text = parsingTool.replaceReferences(text);

        CpogFormula f = null;
        GraphFunc<String, CpogFormula> PGF = null;
        final HashMap<String, VisualVertex> localVertices = new HashMap<String, VisualVertex>();
        try {
            f = CpogExpressionParser.parse(text,
                    PGF = new GraphFunc<String, CpogFormula>() {

                        String name;
                        boolean ref;

                        @Override
                        public CpogFormula eval(String label) {
                            VisualVertex vertex = null;

                            if (vertexMap.containsKey(label)) {
                                vertex = vertexMap.get(label);
                                localVertices.put(label, vertex);
                                return vertex;
                            }

                            vertex = null;

                            // TODO: Optimise!

                            if (!createDuplicates)
                                for (VisualVertex v : visualCpog.getVertices(visualCpog.getCurrentLevel()))
                                    if (v.getLabel().equals(label)) {
                                        vertex = v;
                                        localVertices.put(label, vertex);
                                        break;
                                    }

                            if (vertex == null) {
                                vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
                                vertex.setLabel(label);
                                vertexMap.put(label, vertex);
                                localVertices.put(label, vertex);
                            }
                            return vertex;
                        }

                        @Override
                        public GraphFunc<String, CpogFormula> removeGraphName(String name) {
                            if (vertexMap.containsKey(name)) {
                                vertexMap.remove(name);
                            }
                            return this;
                        }

                        @Override
                        public CpogFormula eval(String label, String boolExpression) throws ParseException {

                            VisualVertex vertex = null;
                            BooleanFormula bf;

                            if (vertexMap.containsKey(label)) {
                                vertex = vertexMap.get(label);
                                if (boolExpression != "") {
                                    if (FormulaToString.toString(vertex.getCondition()) == "") {
                                        try {
                                            vertex.setCondition(parsingTool.parseBool(boolExpression, visualCpog));
                                        } catch (ParseException e) {
                                            throw new ParseException("Boolean error in: " + boolExpression);
                                        }
                                    } else {
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
                                        localVertices.put(label, vertex);
                                        break;
                                    }

                            if (vertex == null) {
                                vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
                                vertex.setLabel(label);
                                vertexMap.put(label, vertex);
                                localVertices.put(label, vertex);
                            }

                            if (boolExpression != "") {
                                if (FormulaToString.toString(vertex.getCondition()) == "") {
                                    try {
                                        bf = parsingTool.parseBool(boolExpression, visualCpog);
                                        vertex.setCondition(bf);
                                    } catch (ParseException e) {
                                        throw new ParseException("Boolean error in: " + boolExpression);
                                    }
                                } else {
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
                            if ((name.contains("{")) && (name.contains("}"))) {
                                ref = true;
                            }
                        }

                        @Override
                        public void setSequenceCondition(CpogFormula formula, String boolForm) {
                            ArcCondition a = new ArcCondition(formula, boolForm);
                            arcConditionList.add(a);
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
            return null;
        } catch (TokenMgrError e) {
            we.cancelMemento();
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Lexical error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (getVertList) {
            for (VisualVertex v :vertexMap.values()) {
                ((VisualPage) visualCpog.getCurrentLevel()).removeWithoutNotify(v);
            }
            return localVertices;
        } else {

        visualCpog.selectNone();
        int n = vertexMap.size();
        int i = 0;
        for (VisualVertex v : vertexMap.values()) {
            visualCpog.addToSelection(v);
        }

        if (!PGF.getRef()) {//If this graph is not for reference
            CpogConnector cc = new CpogConnector(visualCpog);
            f.accept(cc);
            graphMap.put(PGF.getGraphName(), f);

            parsingTool.setArcConditions(arcConditionList, visualCpog, vertexMap);

            LinkedHashSet<Node> roots = getRootNodes(visualCpog, vertexMap.values());//new LinkedHashSet<Node>();

            if (!insertTransitives.getState()) {
                parsingTool.removeTransitives(visualCpog, roots);
            }

            ArrayList<Node> prevSelection = new ArrayList<Node>();
            for (Node n1 : vertexMap.values()) {
                prevSelection.add(n1);
            }


            ArrayList<String> usedReferences = parsingTool.getUsedReferences();

            addUsedReferences(visualCpog, editor, usedReferences, localVertices, prevSelection);

            if (roots.isEmpty()) {
                noRootLayout(vertexMap, n, i);
            } else {
                bfsLayout(visualCpog, roots);
            }

            editor.requestFocus();
            Point2D.Double coordinate = (Double) parsingTool.getLowestVertex(visualCpog);

            if (PGF.getGraphName() != null) {
                insertAsPage(visualCpog, PGF, coordinate, editor);
            } else {
                insertLoose(visualCpog, coordinate);
            }

        } else { //If this graph is for reference only
            String normalForm = getNormalForm(arcConditionList, localVertices);
            parsingTool.addToReferenceList(PGF.getGraphName(), visualCpog, normalForm);
            String graphName = PGF.getGraphName();
            graphName = graphName.replace("{", "");
            graphName = graphName.replace("}", "");
            refVertMap.put(graphName, (HashMap<String, VisualVertex>) localVertices.clone());
            visualCpog.remove(visualCpog.getSelection());
        }

        editor.forceRedraw();

        Collection<Node> prevSelection = visualCpog.getSelection();

        visualCpog.selectAll();

        editor.getMainWindow().zoomFit();

        visualCpog.select(prevSelection);

        we.saveMemento();

        return null;
    }
	}

    public String getNormalForm(HashSet<ArcCondition> arcConditionList, HashMap<String, VisualVertex> localVertices) {
        String normalForm = "";
        Collection<VisualVertex> verts = localVertices.values();
        Iterator<VisualVertex> it = verts.iterator();
        VisualVertex v;
        while (it.hasNext()) {
            v = it.next();
            if (FormulaToString.toString(v.getCondition()).compareTo("1") == 0) {
                normalForm = normalForm + v.getLabel();
            } else {
                normalForm = normalForm + "[" + FormulaToString.toString(v.getCondition()) + "]" + v.getLabel();
            }
            if (it.hasNext()) {
                normalForm = normalForm + " + ";
            }
        }

        Iterator<ArcCondition> it1 = arcConditionList.iterator();
        ArcCondition ac;
        if (!arcConditionList.isEmpty()) {
            normalForm = normalForm + " + ";
        }
        while (it1.hasNext()) {
            ac = it1.next();
            if (ac.getBoolForm().compareTo("") == 0) {
                normalForm = normalForm + " " + CpogFormulaToString.toString(ac.getFormula());
            } else {
                normalForm = normalForm + "[" + ac.getBoolForm() + "](" + CpogFormulaToString.toString(ac.getFormula()) + ")";
            }
            if (it1.hasNext()) {
                normalForm = normalForm + " + ";
            }
        }
        return normalForm;
    }

    public void insertLoose(VisualCPOG visualCpog, Double coordinate) {
        for (Node node : visualCpog.getSelection())
        {
            if (node instanceof VisualVertex)
            {
                VisualVertex v = (VisualVertex) node;
                v.setPosition(new Double(v.getX(), v.getY() + coordinate.getY()));
            }
        }
    }

    public void insertAsPage(VisualCPOG visualCpog, GraphFunc<String, CpogFormula> PGF, Double coordinate, GraphEditor editor) {
        HashSet<VisualPage> pageList = new HashSet<VisualPage>();
        for (Node n0 : visualCpog.getSelection()) {
            if (n0 instanceof VisualPage) {
                pageList.add((VisualPage) n0);
            }
        }

        PageNode pageNode = new PageNode();
        visualCpog.getMathModel().add(pageNode);
        VisualPage page = new VisualPage(pageNode);
        visualCpog.getCurrentLevel().add(page);
        visualCpog.reparent(page, visualCpog, visualCpog.getCurrentLevel(), visualCpog.getSelection());
        visualCpog.select(page);

        page.setLabel(PGF.getGraphName());

        coordinate.setLocation(coordinate.getX(), coordinate.getY() + (page.getBoundingBox().getHeight()/2));
        page.setPosition(coordinate);

        attatchRefEventHandler(visualCpog, page, editor);

    }

    public void bfsLayout(VisualCPOG visualCpog, LinkedHashSet<Node> roots) {
        Iterator<Node> root = roots.iterator();
        ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<Node>();
        double originalX = 0, originalY = 0;
        while(root.hasNext()) {
            q.add(root.next());
            parsingTool.bfsLayout(q, visualCpog, originalX, originalY);
            originalY += 2.5;
        }
    }

    public void noRootLayout(LinkedHashMap<String, VisualVertex> vertexMap, int n, int i) {
        double y = maxY + 2.5;
        for (VisualVertex v : vertexMap.values()) {
            double radius = Math.max(minRadius, expandRadius * n / Math.PI
                    / 2.0);
            Double pos = new Double(maxX + radius
                    * Math.cos(2.0 * Math.PI * i / n), y + radius * Math.sin(2.0 * Math.PI * i / n));
            v.setPosition(pos);
            if (pos.y > highestY) {
                highestY = pos.y;
            }
            i++;
        }
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

    @Override
    public void mouseReleased(GraphEditorMouseEvent e) {
        super.mouseReleased(e);

        VisualVertex v = getVertex(e.getEditor());
        if (v != null) {
            v.sendNotification(new PropertyChangedEvent(v, "position"));
        }
    }


    public LinkedHashSet<Node> getRootNodes(VisualCPOG visualCpog, Collection<VisualVertex> vertexMap) {
        LinkedHashSet<Node> roots = new LinkedHashSet<Node>();
        Set<Connection> arcs;
        Iterator<Connection> it;
        Connection connection;
        boolean second = false;
        for (Node node : vertexMap) {
            arcs = visualCpog.getConnections(node);
            it = arcs.iterator();
            //The following covers root nodes, and nodes with no connections
            while (it.hasNext()) {
                connection = it.next();
                if (!connection.getFirst().equals(node)) {
                    second = true;
                    break;
                }
            }
            if (!second) {
                roots.add(node);
            }
            second = false;
        }
        return roots;
    }

    public void addUsedReferences(VisualCPOG visualCpog, GraphEditor editor, ArrayList<String> usedReferences, HashMap<String, VisualVertex> localVertices, ArrayList<Node> prevSelection) {
        for (String k : usedReferences) {
            visualCpog.selectNone();
            ArrayList<VisualVertex> pageVerts = new ArrayList<VisualVertex>();
            if (refVertMap.containsKey(k)) {
                HashMap<String, VisualVertex> vMap = refVertMap.get(k);
                for(String k1 : vMap.keySet()) {
                    pageVerts.add(localVertices.get(k1));
                    visualCpog.add(localVertices.get(k1));
                    visualCpog.addToSelection(localVertices.get(k1));
                }
                prevSelection.removeAll(pageVerts);
                selectionPageGroup(editor);
                if (visualCpog.getSelection().size() == 1)
                {
                    for (Node n1 : visualCpog.getSelection()) {
                        if (n1 instanceof VisualPage) {
                            VisualPage vp = (VisualPage) n1;
                            vp.setLabel(k);
                            vp.setIsCollapsed(false);
                            prevSelection.add(vp);
                            refPages.add(vp);
                        }
                    }
                }
            }
            visualCpog.addToSelection(prevSelection);
        }
    }

    public void attatchRefEventHandler(final VisualCPOG visualCpog, final Container page, final GraphEditor editor) {
        new HierarchySupervisor() {
        	DefaultHangingConnectionRemover arcRemover = new DefaultHangingConnectionRemover(visualCpog, "CPOG");
        	ArrayList<Node> toBeRemoved = new ArrayList<Node>();
        	String refKey = "";

            @Override
            public void handleEvent(HierarchyEvent e) {
                ArrayList<VisualPage> relaventPages = new ArrayList<VisualPage>();
                if (e instanceof NodesDeletingEvent) {
                    for (Node node: e.getAffectedNodes()) {
                    	if (node instanceof VisualVertex) {
                            final VisualVertex vert = (VisualVertex) node;
                            if (vert.getParent() instanceof VisualPage) {
                                VisualPage page = (VisualPage) vert.getParent();
                                refKey = page.getLabel();
                                relaventPages.addAll(getRefPages(visualCpog, refKey, vert));
                                relaventPages.remove(page);
                                for (VisualPage p : relaventPages) {
                                    for (Node n : p.getChildren()) {
                                        if (n instanceof VisualVertex) {
                                            VisualVertex v = (VisualVertex) n;
                                            if (v.getLabel().compareTo(vert.getLabel()) == 0) {
                                                if (!(e.getAffectedNodes().contains(v))) {
                                                    toBeRemoved.add(v);
                                                }
                                            	break;
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
                    for (Node n : toBeRemoved) {
                        if (n.getParent() != null) {
                            visualCpog.removeWithoutNotify(n);
                            arcRemover.handleEvent(new NodesDeletingEvent(n.getParent(), n));
                        }
                    }

                    //Update referenced expression
                    if (relaventPages.size() > 0) {
                        Container previousLevel = visualCpog.getCurrentLevel();
                        Collection<Node> selection = visualCpog.getSelection();


                        VisualPage page = relaventPages.get(0);
                        visualCpog.setCurrentLevel((Container) page.getParent());
                        visualCpog.select(page);

                        String newExpression = parsingTool.getExpressionFromGraph(visualCpog);
                        newExpression = newExpression.replace("\n", "");
                        while (newExpression.startsWith(" ")) {
                            newExpression = newExpression.substring(1);
                        }
                        while (newExpression.endsWith(" "))  {
                            newExpression = newExpression.substring(0, newExpression.length() - 1);
                        }

                        refMap.remove(page.getLabel());
                        int eqLocation = 0;
                        eqLocation = newExpression.indexOf('=');
                        refMap.put(page.getLabel(), newExpression.substring(eqLocation + 1));

                        //newExpression = page.getLabel() + " = " + newExpression;
                        refVertMap.remove(page.getLabel());
                        refVertMap.put(page.getLabel(), (HashMap<String, VisualVertex>) insertExpression(editor, newExpression, true, true).clone());

                        visualCpog.setCurrentLevel(previousLevel);
                        visualCpog.select(selection);
                    }


            }
            }
        }.attach(page);

        final class StateSupervisorExtension extends StateSupervisor {
            @Override
            public void handleEvent(StateEvent e) {
                if (e instanceof PropertyChangedEvent) {
                    PropertyChangedEvent pce = (PropertyChangedEvent) e;
                    if (((PropertyChangedEvent) e).getPropertyName().compareTo("position") == 0)
                    {
                        if (pce.getSender() instanceof VisualVertex) {
                            VisualVertex v = (VisualVertex) pce.getSender();
                            if (v.getParent() instanceof VisualPage) {
                                VisualPage page = (VisualPage) v.getParent();

                                String refKey = page.getLabel();

                                ArrayList<VisualPage> refPages = getRefPages(visualCpog, refKey, v);
                                refPages.remove(page);

                                for (VisualPage p : refPages) {
                                    for (Node n : p.getChildren()) {
                                        if ((n instanceof VisualVertex) && (((VisualVertex) n).getLabel().compareTo(v.getLabel()) == 0)) {
                                            ((VisualVertex) n).setPosition(v.getPosition());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        new StateSupervisorExtension().attach(page);
    }

    public ArrayList<VisualPage> getRefPages(VisualCPOG visualCpog, String refKey, VisualVertex v) {
        ArrayList<VisualPage> result = new ArrayList<VisualPage>();


        for (VisualPage p : refPages) {
        	if (p.getLabel().compareTo(refKey) == 0) {
        		result.add(p);

        	}
        }
        return result;
    }

    public VisualVertex getVertex(GraphEditor editor) {

        WorkspaceEntry we = editor.getWorkspaceEntry();
        final VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();

        if (visualCpog.getSelection().size() == 1) {
            for (Node n : visualCpog.getSelection()) {
                if (n instanceof VisualVertex) {
                    return (VisualVertex) n;
                }
            }
        }

        return null;
    }


}
