package org.workcraft.plugins.cpog.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.gui.tools.editors.AbstractInplaceEditor;
import org.workcraft.gui.tools.editors.LabelInplaceEditor;
import org.workcraft.observation.*;
import org.workcraft.plugins.cpog.*;
import org.workcraft.plugins.cpog.formula.CpogConnector;
import org.workcraft.plugins.cpog.formula.CpogFormula;
import org.workcraft.plugins.cpog.formula.CpogFormulaToString;
import org.workcraft.plugins.cpog.formula.GraphFunc;
import org.workcraft.plugins.cpog.formula.jj.CpogFormulaParser;
import org.workcraft.plugins.cpog.formula.jj.ParseException;
import org.workcraft.plugins.cpog.formula.jj.TokenMgrError;
import org.workcraft.plugins.cpog.observers.CpogHangingConnectionRemover;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unchecked")
public class CpogSelectionTool extends SelectionTool {

    private static final String GROUP_ICON = "images/selection-page.svg";
    private static final String GROUP_HINT = "Combine selection as a scenario (Alt-G)";

    private static final double minRadius = 2.0;
    private static final double expandRadius = 2.0;

    private static final double maxX = 0;
    private static final double maxY = 0;
    private static final int xpos = 0;

    private Point2D coordinate = new Point2D.Double(0, 0);
    private final HashMap<String, CpogFormula> graphMap = new HashMap<>();
    private final HashMap<String, Variable> variableMap = new HashMap<>();
    private final HashMap<String, GraphReference> referenceMap = new HashMap<>();
    private final HashMap<String, Point2D> prevPoints = new HashMap<>();
    private double highestY = 0; // Sets first graph at y co-ordinate of 0
    private JTextArea expressionText;
    private Checkbox insertTransitives;

    private final CpogParsingTool parsingTool = new CpogParsingTool(variableMap, xpos, referenceMap);
    private final ArrayList<VisualPage> refPages = new ArrayList<>();
    private GraphEditor editor;
    private int scenarioNo = 0;
    private JPanel panel;

    private final class CpogGraphFunc implements GraphFunc<String, CpogFormula> {
        private final VisualCpog visualCpog;
        private final HashSet<ArcCondition> arcConditionList;
        private final HashMap<String, VisualVertex> localVertices;
        private final LinkedHashMap<String, VisualVertex> vertexMap;

        private CpogGraphFunc(VisualCpog visualCpog, HashSet<ArcCondition> arcConditionList,
                HashMap<String, VisualVertex> localVertices, LinkedHashMap<String, VisualVertex> vertexMap) {

            this.visualCpog = visualCpog;
            this.arcConditionList = arcConditionList;
            this.localVertices = localVertices;
            this.vertexMap = vertexMap;
        }

        @Override
        public CpogFormula eval(String label) {
            VisualVertex vertex = null;

            if (vertexMap.containsKey(label)) {
                vertex = vertexMap.get(label);
                localVertices.put(label, vertex);
                return vertex;
            }
            vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
            vertex.setLabel(label);
            vertexMap.put(label, vertex);
            localVertices.put(label, vertex);
            return vertex;
        }

        @Override
        public GraphFunc<String, CpogFormula> removeGraphName(String name) {
            vertexMap.remove(name);
            return this;
        }

        @SuppressWarnings("PMD.AvoidThrowingNewInstanceOfSameException")
        @Override
        public CpogFormula eval(String label, String boolExpression) throws ParseException {
            VisualVertex vertex = null;
            if (vertexMap.containsKey(label)) {
                vertex = vertexMap.get(label);
                String s = StringGenerator.toString(vertex.getCondition());
                if (!boolExpression.isEmpty()) {
                    if (s.isEmpty()) {
                        try {
                            vertex.setCondition(parsingTool.parseBool(boolExpression, visualCpog));
                        } catch (org.workcraft.formula.jj.ParseException e) {
                            throw new ParseException("Boolean error in: " + boolExpression);
                        }
                    } else {
                        try {
                            BooleanFormula bool = parsingTool.parseBool(s + "|" + boolExpression, visualCpog);
                            vertex.setCondition(bool);
                        } catch (org.workcraft.formula.jj.ParseException e) {
                            throw new ParseException("Boolean error in: " + boolExpression);
                        }
                    }
                }
                return vertex;
            }

            vertex = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
            vertex.setLabel(label);
            vertexMap.put(label, vertex);
            localVertices.put(label, vertex);

            if (!boolExpression.isEmpty()) {
                try {
                    BooleanFormula bf = parsingTool.parseBool(boolExpression, visualCpog);
                    vertex.setCondition(bf);
                } catch (org.workcraft.formula.jj.ParseException e) {
                    throw new ParseException("Boolean error in: " + boolExpression);
                }
            }
            return vertex;
        }

        @Override
        public void setSequenceCondition(CpogFormula formula, String boolForm) {
            ArcCondition a = new ArcCondition(formula, boolForm);
            arcConditionList.add(a);
        }
    }

    private final class RenderTypeChangedHandler extends StateSupervisor {
        @Override
        public void handleEvent(StateEvent e) {
            if (e instanceof PropertyChangedEvent pce) {
                if (VisualVertex.PROPERTY_RENDER_TYPE.equals(pce.getPropertyName())) {
                    correctConnectionLengths((VisualVertex) pce.getSender());
                }
            }
        }
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }
        this.editor = editor;

        expressionText = new JTextArea();
        expressionText.setMargin(SizeHelper.getTextMargin());
        expressionText.setLineWrap(false);
        expressionText.setEditable(true);
        expressionText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, SizeHelper.getMonospacedFontSize()));
        JScrollPane expressionScroll = new JScrollPane(expressionText);

        JPanel buttonPanel = new JPanel();

        JButton btnInsert = new JButton("Insert");
        btnInsert.addActionListener(event -> insertAction());
        buttonPanel.add(btnInsert);

        insertTransitives = new Checkbox("Insert transitives", false);
        buttonPanel.add(insertTransitives);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(expressionScroll, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JButton groupPageButton = GuiUtils.createIconButton(GROUP_ICON, GROUP_HINT, event -> groupPageAction(editor));

        JPanel groupPanel = getGroupPanel();
        if (groupPanel != null) {
            groupPanel.add(groupPageButton, 1);
        }

        final VisualCpog visualCpog = (VisualCpog) editor.getWorkspaceEntry().getModelEntry().getVisualModel();
        new RenderTypeChangedHandler().attach(visualCpog.getRoot());
        return panel;
    }

    private void insertAction() {
        int prevLineEnd = 0;
        ArrayList<String> expressions = new ArrayList<>();
        editor.getWorkspaceEntry().captureMemento();
        try {
            for (int i = 0; i < expressionText.getLineCount(); i++) {
                String exp1 = expressionText.getText().substring(prevLineEnd, expressionText.getLineEndOffset(i));

                exp1 = exp1.replace("\n", "");
                exp1 = exp1.replace("\t", " ");

                if (!exp1.isEmpty()) {
                    expressions.add(exp1);
                }

                prevLineEnd = expressionText.getLineEndOffset(i);
            }
            WorkspaceEntry we = editor.getWorkspaceEntry();
            VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);
            StringBuilder exp2 = new StringBuilder();
            coordinate = getLowestVertex(visualCpog);
            coordinate.setLocation(coordinate.getX(), coordinate.getY() + 2);
            for (String s : expressions) {
                if (!s.contains("=")) {
                    exp2.append(' ').append(s);
                } else {
                    if (!exp2.isEmpty()) {
                        insertExpression(exp2.toString(), visualCpog, false, true, false);
                        exp2 = new StringBuilder();
                    }
                    exp2 = new StringBuilder(s);
                }
            }
            if (!exp2.isEmpty()) {
                insertExpression(exp2.toString(), visualCpog, false, true, false);
            }
            editor.getWorkspaceEntry().saveMemento();
        } catch (BadLocationException e1) {
            editor.getWorkspaceEntry().cancelMemento();
            e1.printStackTrace();
        }
    }

    private void groupPageAction(final GraphEditor editor) {
        VisualCpog visualCpog = (VisualCpog) editor.getWorkspaceEntry().getModelEntry().getVisualModel();
        visualCpog.groupScenarioPageSelection("scenario" + scenarioNo);
        scenarioNo++;
        editor.requestFocus();
    }

    private JPanel getGroupPanel() {
        JPanel groupPanel = null;
        for (Component component1 : panel.getComponents()) {
            if (!(component1 instanceof JPanel panel1)) continue;
            for (Component component2 : panel1.getComponents()) {
                if (!(component2 instanceof JPanel panel2)) continue;
                Component[] c = panel2.getComponents();
                for (Component value : c) {
                    if (!(value instanceof JButton button)) continue;
                    if ((button.getToolTipText() != null) && button.getToolTipText().startsWith("Group selection (")) {
                        groupPanel = panel2;
                    }
                }
            }
        }
        return groupPanel;
    }

    public HashMap<String, VisualVertex> insertExpression(String text, final VisualCpog visualCpog,
            boolean getVertList, boolean zoomFit, boolean blockTransitiveRemoval) {

        WorkspaceEntry we = editor.getWorkspaceEntry();
        String name = "";
        visualCpog.setCurrentLevel(visualCpog.getRoot());

        final LinkedHashMap<String, VisualVertex> vertexMap = new LinkedHashMap<>();
        final HashSet<ArcCondition> arcConditionList = new HashSet<>();
        text = text.replace("\n", "");
        text = parsingTool.replaceReferences(text);

        if (text.contains("=")) {
            name = text.substring(0, text.indexOf("="));
            name = name.trim();
            text = text.substring(text.indexOf("=") + 1);
            text = text.trim();
        }

        CpogFormula f = null;
        final HashMap<String, VisualVertex> localVertices = new HashMap<>();
        try {
            f = CpogFormulaParser.parse(text, new CpogGraphFunc(visualCpog, arcConditionList, localVertices, vertexMap));
        } catch (ParseException e) {
            we.cancelMemento();
            DialogUtils.showError(e.getMessage(), "Parse error");
            return null;
        } catch (TokenMgrError e) {
            we.cancelMemento();
            DialogUtils.showError(e.getMessage(), "Lexical error");
            return null;
        }

        if (getVertList) {
            for (VisualVertex v : vertexMap.values()) {
                visualCpog.removeWithoutNotify(v);
            }
            return localVertices;
        } else {

            visualCpog.selectNone();
            int n = vertexMap.size();
            int i = 0;

            for (VisualVertex v : vertexMap.values()) {
                visualCpog.addToSelection(v);
            }

            CpogConnector cc = new CpogConnector(visualCpog);
            f.accept(cc);
            VisualPage inserted = null;

            graphMap.put(name, f);

            parsingTool.setArcConditions(arcConditionList, visualCpog, vertexMap);

            LinkedHashSet<VisualNode> roots = getRootNodes(visualCpog, vertexMap.values());

            if (!(insertTransitives.getState()) && (!blockTransitiveRemoval)) {
                boolean[][] c = parsingTool.convertToArrayForm(vertexMap.values(), visualCpog);
                parsingTool.computeTransitiveClosure(c);
                if (!parsingTool.hasSelfLoops(c)) {
                    boolean[][] t = parsingTool.findTransitives(c);
                    parsingTool.convertFromArrayForm(t, vertexMap.values(), visualCpog);
                }
            }

            ArrayList<VisualNode> prevSelection = new ArrayList<>(vertexMap.values());

            ArrayList<String> usedReferences = parsingTool.getUsedReferences();

            addUsedReferences(visualCpog, editor, usedReferences, localVertices, prevSelection);

            if (roots.isEmpty()) {
                noRootLayout(vertexMap, n, i);
            } else {
                bfsLayout(visualCpog, roots);
            }

            editor.requestFocus();

            if (!name.isEmpty()) {
                inserted = insertAsPage(visualCpog, name, coordinate, editor);
                coordinate = new Point2D.Double(coordinate.getX(), coordinate.getY() + 2);
            } else {
                insertLoose(visualCpog, coordinate);
            }
            if (inserted != null) {
                String normalForm = getNormalForm(arcConditionList, localVertices);
                String graphName = name;
                graphName = graphName.replace("{", "");
                graphName = graphName.replace("}", "");
                roots = getRootNodes(visualCpog, localVertices.values());
                bfsLayout(visualCpog, roots);
                referenceMap.remove(graphName);
                GraphReference g = new GraphReference(normalForm, (HashMap<String, VisualVertex>) localVertices.clone());
                g.addRefPage(inserted);
                referenceMap.put(graphName, g);
            }
            Collection<VisualNode> oldSelection = visualCpog.getSelection();
            visualCpog.selectNone();
            // Do not allow zoomFit when creating a new CPOG model, such as when extracting concurrency
            if (zoomFit) {
                editor.zoomFit();
            }
            visualCpog.select(oldSelection);
            return null;
        }
    }

    public String getNormalForm(HashSet<ArcCondition> arcConditionList, HashMap<String, VisualVertex> localVertices) {
        StringBuilder normalForm = new StringBuilder();
        Collection<VisualVertex> verts = localVertices.values();
        Iterator<VisualVertex> it = verts.iterator();
        VisualVertex v;
        while (it.hasNext()) {
            v = it.next();
            if ("1".equals(StringGenerator.toString(v.getCondition()))) {
                normalForm.append(v.getLabel());
            } else {
                normalForm.append("[").append(StringGenerator.toString(v.getCondition())).append("]").append(v.getLabel());
            }
            if (it.hasNext()) {
                normalForm.append(" + ");
            }
        }

        Iterator<ArcCondition> it1 = arcConditionList.iterator();
        ArcCondition ac;
        if (!arcConditionList.isEmpty()) {
            normalForm.append(" + ");
        }
        while (it1.hasNext()) {
            ac = it1.next();
            if (ac.getBoolForm().isEmpty()) {
                normalForm.append(' ').append(CpogFormulaToString.toString(ac.getFormula()));
            } else {
                normalForm.append('[').append(ac.getBoolForm()).append("](").append(CpogFormulaToString.toString(ac.getFormula())).append(')');
            }
            if (it1.hasNext()) {
                normalForm.append(" + ");
            }
        }
        return normalForm.toString();
    }

    public void insertLoose(VisualCpog visualCpog, Point2D coordinate) {
        for (VisualNode node : visualCpog.getSelection()) {
            if (node instanceof VisualVertex v) {
                v.setPosition(new Point2D.Double(v.getX(), v.getY() + coordinate.getY()));
            }
        }
    }

    public VisualPage insertAsPage(VisualCpog visualCpog, String graphName, Point2D coordinate, GraphEditor editor) {
        HashSet<VisualScenarioPage> pageList = new HashSet<>();
        for (VisualNode n0 : visualCpog.getSelection()) {
            if (n0 instanceof VisualScenarioPage) {
                pageList.add((VisualScenarioPage) n0);
            }
        }

        PageNode pageNode = new PageNode();
        visualCpog.getMathModel().add(pageNode);
        VisualScenarioPage page = new VisualScenarioPage(pageNode);
        visualCpog.getCurrentLevel().add(page);
        includeArcsInPage(visualCpog);

        Container container = visualCpog.getCurrentLevel();
        HashSet<VisualNode> nodes = new HashSet<>();
        for (VisualNode n : visualCpog.getSelection()) {
            if (!(n.getParent().equals(container))) {
                visualCpog.reparent(page, visualCpog, container, nodes);
                container = (Container) n.getParent();
                nodes.clear();
            }
            nodes.add(n);
        }

        visualCpog.reparent(page, visualCpog, container, nodes);
        for (VisualComponent c : page.getComponents()) {
            if (c instanceof VisualPage) {
                if (c.getComponents().isEmpty()) {
                    visualCpog.remove(c);
                }
            }
        }
        visualCpog.select(page);

        page.setLabel(graphName);
        Point2D.Double pageLocation = new Point2D.Double(coordinate.getX(), coordinate.getY() + (page.getBoundingBox().getHeight() / 2));
        coordinate.setLocation(coordinate.getX(), coordinate.getY() + page.getBoundingBox().getHeight() + 1);
        page.setPosition(pageLocation);

        attatchRefEventHandler(visualCpog, page, editor);

        return page;

    }

    public void bfsLayout(VisualCpog visualCpog, LinkedHashSet<VisualNode> roots) {
        Iterator<VisualNode> root = roots.iterator();
        ConcurrentLinkedQueue<VisualNode> q = new ConcurrentLinkedQueue<>();
        double originalX = 0;
        double originalY = 0;
        while (root.hasNext()) {
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
            Point2D pos = new Point2D.Double(maxX + radius
                    * Math.cos(2.0 * Math.PI * i / n), y + radius * Math.sin(2.0 * Math.PI * i / n));
            v.setPosition(pos);
            if (pos.getY() > highestY) {
                highestY = pos.getY();
            }
            i++;
        }
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;

        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
            VisualModel model = e.getEditor().getModel();
            VisualNode node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node != null) {
                if (node instanceof VisualVariable var) {
                    var.toggle();
                    processed = true;
                } else if (node instanceof VisualVertex vertex) {
                    AbstractInplaceEditor textEditor = new LabelInplaceEditor(editor, vertex) {
                        @Override
                        public void processResult(String text) {
                            super.processResult(text);
                            correctConnectionLengths(vertex);
                        }
                    };
                    textEditor.edit(vertex.getLabel(), vertex.getLabelFont(),
                            vertex.getLabelOffset(), vertex.getLabelAlignment(), false);
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

        WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
        final VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);

        for (VisualNode n : visualCpog.getSelection()) {
            if (n instanceof VisualVertex) {
                n.sendNotification(new PropertyChangedEvent(n, "position"));
            }
        }
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
        super.startDrag(e);
        WorkspaceEntry we = e.getEditor().getWorkspaceEntry();
        final VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);

        prevPoints.clear();
        for (VisualNode n : visualCpog.getSelection()) {
            if (n instanceof VisualVertex v) {
                prevPoints.put(v.getLabel(), new Point2D.Double(v.getPosition().getX(), v.getPosition().getY()));
            }
        }
    }

    public LinkedHashSet<VisualNode> getRootNodes(VisualCpog visualCpog, Collection<VisualVertex> vertexMap) {
        LinkedHashSet<VisualNode> roots = new LinkedHashSet<>();
        Set<VisualConnection> arcs;
        Iterator<VisualConnection> it;
        VisualConnection connection;
        boolean second = false;
        for (VisualNode node : vertexMap) {
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

    public void addUsedReferences(VisualCpog visualCpog, GraphEditor editor, ArrayList<String> usedReferences,
            HashMap<String, VisualVertex> localVertices, ArrayList<VisualNode> prevSelection) {

        for (String k : usedReferences) {
            visualCpog.selectNone();
            ArrayList<VisualVertex> pageVerts = new ArrayList<>();
            if (referenceMap.containsKey(k)) {
                GraphReference g = referenceMap.get(k);
                HashMap<String, VisualVertex> vMap = g.getVertMap();
                for (String k1 : vMap.keySet()) {
                    localVertices.get(k1).setPosition(new Point2D.Double(vMap.get(k1).getX(), vMap.get(k1).getY()));
                    pageVerts.add(localVertices.get(k1));
                    if (visualCpog.getVertices(visualCpog.getCurrentLevel()).contains(localVertices.get(k1))) {
                        visualCpog.add(localVertices.get(k1));
                    }
                    visualCpog.addToSelection(localVertices.get(k1));
                }
                prevSelection.removeAll(pageVerts);
                includeArcsInPage(visualCpog);
                pageSelection(editor);
                if (visualCpog.getSelection().size() == 1) {
                    for (VisualNode n1 : visualCpog.getSelection()) {
                        if (n1 instanceof VisualPage vp) {
                            vp.setLabel(k);
                            vp.setIsCollapsed(false);
                            vp.setParent(visualCpog.getCurrentLevel());
                            prevSelection.add(vp);
                            referenceMap.get(k).addRefPage(vp);
                            refPages.add(vp);
                        }
                    }
                }
            }
            visualCpog.addToSelection(prevSelection);
        }
    }

    public void attatchRefEventHandler(final VisualCpog visualCpog, final Container page, final GraphEditor editor) {
        new HierarchySupervisor() {
            String refKey = "";
            final ArrayList<VisualNode> toBeRemoved = new ArrayList<>();

            @Override
            public void handleEvent(HierarchyEvent e) {
                ArrayList<VisualPage> relaventPages = new ArrayList<>();
                if (e instanceof NodesDeletingEvent) {
                    for (Node node : e.getAffectedNodes()) {
                        if (node instanceof VisualVertex vert) {
                            if (!(vert.getParent() instanceof VisualScenarioPage)) {
                                if (vert.getParent() instanceof VisualPage page) {
                                    refKey = page.getLabel();
                                    relaventPages.addAll(referenceMap.get(page.getLabel()).getRefPages());
                                    relaventPages.remove(page);
                                    for (VisualPage p : relaventPages) {
                                        for (Node n : p.getChildren()) {
                                            if (n instanceof VisualVertex v) {
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
                            CpogHangingConnectionRemover arcRemover = new CpogHangingConnectionRemover(visualCpog);
                            for (VisualNode n : toBeRemoved) {
                                arcRemover.handleEvent(new NodesDeletingEvent(n.getParent(), n));
                                visualCpog.removeWithoutNotify(n);
                            }

                        } else if (node instanceof VisualArc a) {
                            if (a.getFirst().getParent().equals(a.getSecond().getParent())) {
                                if ((a.getFirst().getParent() instanceof VisualPage) || (a.getFirst().getParent() instanceof VisualScenarioPage)) {
                                    VisualPage vp = (VisualPage) a.getFirst().getParent();
                                    String first = ((VisualVertex) a.getFirst()).getLabel();
                                    String second = ((VisualVertex) a.getSecond()).getLabel();
                                    refKey = vp.getLabel();
                                    relaventPages.addAll(referenceMap.get(refKey).getRefPages());
                                    relaventPages.remove(vp);
                                    for (VisualPage p : relaventPages) {
                                        for (Node n : p.getChildren()) {
                                            if (n instanceof VisualVertex f) {
                                                if (f.getLabel().equals(first)) {
                                                    for (Node n1 : p.getChildren()) {
                                                        if (n1 instanceof VisualVertex s) {
                                                            if (s.getLabel().equals(second)) {
                                                                VisualConnection c = visualCpog.getConnection(f, s);
                                                                if (!(e.getAffectedNodes().contains(c))) {
                                                                    toBeRemoved.add(c);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            for (VisualNode n : toBeRemoved) {
                                visualCpog.removeFromSelection(n);
                                visualCpog.removeWithoutNotify(n);
                                while (n.getParent() != null) {
                                    try {
                                        this.wait(5);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        }

                    }
                }

                toBeRemoved.clear();
                //updateReferenceNormalForm(relaventPages, visualCpog, editor);
            }

        }.attach(page);

        final class StateSupervisorExtension extends StateSupervisor {
            @Override
            public void handleEvent(StateEvent e) {
                if (e instanceof PropertyChangedEvent pce) {
                    if ("position".equals(pce.getPropertyName())) {
                        if ((pce.getSender() instanceof VisualVertex v) && !(pce.getSender().getParent() instanceof VisualScenarioPage)) {
                            double xDiff = 0;
                            double yDiff = 0;
                            if (prevPoints.get(v.getLabel()) != null) {
                                xDiff = v.getPosition().getX() - prevPoints.get(v.getLabel()).getX();
                                yDiff = v.getPosition().getY() - prevPoints.get(v.getLabel()).getY();
                                prevPoints.remove(v.getLabel());
                            }

                            if (v.getParent() instanceof VisualPage page) {

                                String refKey = page.getLabel();

                                GraphReference g = referenceMap.get(page.getLabel());
                                if (g != null) {
                                    g.updateVertexPosition(v.getLabel(), xDiff, yDiff);

                                    ArrayList<VisualPage> refPages = getRefPages(visualCpog, refKey, v);
                                    refPages.remove(page);

                                    for (VisualPage p : refPages) {
                                        for (Node n : p.getChildren()) {
                                            if ((n instanceof VisualVertex vert) && (vert.getLabel().compareTo(v.getLabel()) == 0)) {
                                                vert.setPosition(new Point2D.Double(vert.getPosition().getX() + xDiff, vert.getPosition().getY() + yDiff));
                                            }
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

    public ArrayList<VisualPage> getRefPages(VisualCpog visualCpog, String refKey, VisualVertex v) {
        ArrayList<VisualPage> result = new ArrayList<>();

        for (VisualPage p : refPages) {
            if (p.getLabel().compareTo(refKey) == 0) {
                result.add(p);

            }
        }
        return result;
    }

    public void includeArcsInPage(VisualCpog visualCpog) {
        HashSet<VisualNode> arcs = new HashSet<>();
        for (VisualNode n : visualCpog.getSelection()) {
            arcs.addAll(visualCpog.getConnections(n));
        }
        visualCpog.addToSelection(arcs);
    }

    public void insertEventLog(VisualCpog visualCpog, int i, String[] events, double yPos) {
        final LinkedHashMap<String, VisualVertex> vertexMap = new LinkedHashMap<>();
        VisualVertex vertex1;
        VisualVertex vertex2;
        for (int c = 0; c < events.length - 1; c++) {
            String first = "";
            String second = "";

            first = events[c];
            second = events[c + 1];

            if (vertexMap.containsKey(first)) {
                vertex1 = vertexMap.get(first);
            } else {
                vertex1 = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
                vertex1.setLabel(first);
                vertexMap.put(first, vertex1);
            }

            if (vertexMap.containsKey(second)) {
                int d = 1;
                while (vertexMap.containsKey(second + "_" + d)) {
                    d++;
                }
                vertex2 = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
                vertex2.setLabel(second + "_" + d);
                vertexMap.put(second + "_" + d, vertex2);

                events[c + 1] = second + "_" + d;
            } else {
                vertex2 = visualCpog.createVisualVertex(visualCpog.getCurrentLevel());
                vertex2.setLabel(second);
                vertexMap.put(second, vertex2);
            }

            visualCpog.connect(vertex1, vertex2);

        }

        double xPos = vertexMap.size();
        xPos *= 2.5;
        xPos = 0 - xPos / 2;

        PageNode pageNode = new PageNode();
        visualCpog.getMathModel().add(pageNode);
        VisualScenarioPage page = new VisualScenarioPage(pageNode);
        visualCpog.getCurrentLevel().add(page);
        page.setLabel("t" + i);

        Container container = visualCpog.getCurrentLevel();
        HashSet<VisualNode> nodes = new HashSet<>();

        visualCpog.selectNone();
        for (VisualVertex v : vertexMap.values()) {
            v.setPosition(new Point2D.Double(xPos, yPos));
            xPos += 2.5;
            nodes.add(v);
        }

        visualCpog.reparent(page, visualCpog, container, nodes);
        includeArcsInPage(visualCpog);

    }

    public Point2D getLowestVertex(VisualCpog visualCpog) {
        return parsingTool.getLowestVertex(visualCpog);
    }

    private void correctConnectionLengths(VisualVertex vertex) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        ArrayList<VisualNode> cons = CpogParsingTool.getChildren(visualCpog, vertex);
        cons.addAll(CpogParsingTool.getParents(visualCpog, vertex));
        for (VisualNode n : cons) {
            VisualNode f;
            VisualNode s;
            if (visualCpog.getConnection(n, vertex) != null) {
                f = n;
                s = vertex;
            } else {
                f = vertex;
                s = n;
            }
            VisualConnection c = visualCpog.getConnection(f, s);
            VisualArc a = (VisualArc) c;
            BooleanFormula b = a.getCondition();
            visualCpog.remove(c);
            try {
                a = (VisualArc) visualCpog.connect(f, s);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
            a.setCondition(b);
        }
    }

    public boolean insertCpogFromFile(File f) {
        Scanner fileIn;
        try {
            fileIn = new Scanner(f);
        } catch (FileNotFoundException e1) {
            DialogUtils.showError(e1.getMessage());
            return false;
        }

        WorkspaceEntry we = editor.getWorkspaceEntry();
        VisualCpog visualCpog = WorkspaceUtils.getAs(we, VisualCpog.class);

        coordinate = getLowestVertex(visualCpog);
        coordinate.setLocation(coordinate.getX(), coordinate.getY() + 2);

        StringBuilder expression = new StringBuilder();

        while (fileIn.hasNextLine()) {
            expression.append(fileIn.nextLine()).append('\n');
        }
        fileIn.close();


        StringBuilder exp = new StringBuilder();
        coordinate = getLowestVertex(visualCpog);
        coordinate.setLocation(coordinate.getX(), coordinate.getY() + 2);
        String[] expressions = expression.toString().split("\n");
        for (String s : expressions) {
            if (!s.contains("=")) {
                exp.append(' ').append(s);
            } else {
                if (!exp.isEmpty()) {
                    insertExpression(exp.toString(), visualCpog, false, true, false);
                }
                exp = new StringBuilder(s);
            }
        }
        if (!exp.isEmpty()) {
            insertExpression(exp.toString(), visualCpog, false, true, false);
        }

        return true;
    }

    public void setExpressionText(String exp) {
        expressionText.setText(exp);
    }

}
