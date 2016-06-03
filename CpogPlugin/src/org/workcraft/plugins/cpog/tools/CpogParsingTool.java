package org.workcraft.plugins.cpog.tools;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.gui.DesktopApi;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VisualArc;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.cpog.VisualScenarioPage;
import org.workcraft.plugins.cpog.VisualVariable;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.util.Func;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogParsingTool {

    private static final int MAX_SCENARIOS_LINUX = 680;
    private static final int MAX_SCENARIOS_OTHER_OS = 340;

    public CpogParsingTool(HashMap<String, Variable> variableMap, int xpos, HashMap<String, GraphReference> refMap) {
        this.variableMap = variableMap;
        this.xpos = xpos;
        this.refMap = refMap;
    }

    private final HashMap<String, Variable> variableMap;
    private int xpos;
    private final HashMap<String, GraphReference> refMap;
    private ArrayList<String> usedReferences;

    public BooleanFormula parseBool(String bool, final VisualCpog visualCpog) throws ParseException {
        Func<String, BooleanVariable> boolVars = new Func<String, BooleanVariable>() {
            public BooleanVariable eval(final String label) {
                if (variableMap.containsKey(label)) {
                    if (!visualCpog.getVariables(visualCpog.getRoot()).contains(variableMap.get(label))) {
                        if (variableMap.get(label).getParent() != null) {
                            return variableMap.get(label);
                        } else  variableMap.remove(label);
                    } else {
                        variableMap.remove(label);
                    }
                }

                VisualVariable visVar = visualCpog.createVisualVariable();
                visVar.setLabel(label);
                visVar.setPosition(new Point2D.Double(xpos, -2));
                xpos++;
                variableMap.put(label, visVar.getMathVariable());

                return variableMap.get(label);
            }
        };

        BooleanFormula boolForm;

        try {
            boolForm = BooleanFormulaParser.parse(bool, boolVars);
        } catch (ParseException e) {
            throw new ParseException("Boolean error in: " + bool);
        }
        return boolForm;
    }

    public void bfsLayout(Queue<Node> q, VisualCpog visualCpog, double originalX, double originalY) {

        ArrayList<ArrayList<Node>> outer = new ArrayList<>();
        HashSet<VisualPage> pages = new HashSet<>();
        Node current = q.remove();
        ArrayList<Node> children = getChildren(visualCpog, current);

        outer.add(new ArrayList<Node>());
        outer.get(0).add(current);
        outer.add(new ArrayList<Node>());

        for (Node child : children) {
            q.add(child);
            outer.get(1).add(child);
        }

        findAllChildren(q, visualCpog, outer, pages);

        positionNodes(originalX, originalY, outer);

    }

    public double positionNodes(double originalX, double originalY, ArrayList<ArrayList<Node>> outer) {
        Double centre = new Double(0, originalY);

        double x = originalX;
        double y = 0;

        Iterator<ArrayList<Node>> it = outer.iterator();

        while (it.hasNext()) {
            ArrayList<Node> inner = it.next();
            if (inner.size() > 1) {
                y = centre.getY() - (inner.size() / 2);
            } else {
                y = centre.getY();
            }
            for (Node n : inner) {
                if (n instanceof VisualVertex) {
                    VisualVertex v = (VisualVertex) n;
                    if ((v.getParent() instanceof VisualPage) && (refMap.containsKey(((VisualPage) v.getParent()).getLabel()))) {
                        VisualPage p = (VisualPage) v.getParent();
                        Point2D.Double newPosition = new
                                Point2D.Double(refMap.get(p.getLabel()).getVertMap().get(v.getLabel()).getX(),
                                        refMap.get(p.getLabel()).getVertMap().get(v.getLabel()).getY());
                        v.setPosition(newPosition);
                    } else {
                        v.setPosition(new Double(x, y));
                    }
                }
                y += 1.5;
            }

            if (it.hasNext()) {
                x += 2.5;
            }
        }
        y += 2.5;
        return x;
    }

    public void findAllChildren(Queue<Node> q, VisualCpog visualCpog, ArrayList<ArrayList<Node>> outer, HashSet<VisualPage> pages) {
        Node current;
        ArrayList<Node> children;
        int index = 0;
        HashSet<Node> visitedNodes = new HashSet<>();
        while (!q.isEmpty()) {
            current = q.remove();
            if (!visitedNodes.contains(current)) {
                visitedNodes.add(current);
                index = findVertex(outer, current);
                if ((current.getParent() instanceof VisualScenarioPage) | (current.getParent() instanceof VisualPage)) {
                    VisualPage vp = (VisualPage) current.getParent();
                    pages.add(vp);
                }
                children = getChildren(visualCpog, current);
                for (Node child : children) {
                    q.add(child);
                    addNode(child, index + 1, outer);
                }
            }
        }
    }

    public static ArrayList<Node> getChildren(VisualCpog visualCpog, Node node) {
        ArrayList<Node> children = new ArrayList<>();
        HashSet<VisualArc> arcs = getAllArcs(visualCpog.getRoot(), visualCpog);

        for (VisualArc arc : arcs) {
            if (arc.getFirst().equals(node)) {
                children.add(arc.getSecond());
            }
        }

        return children;
    }

    public static HashSet<Node> getParents(VisualCpog visualCpog, Node node) {
        HashSet<Node> parents = new HashSet<>();
        HashSet<VisualArc> arcs = getAllArcs(visualCpog.getRoot(), visualCpog);

        for (VisualArc arc : arcs) {
            if (arc.getSecond().equals(node)) {
                parents.add(arc.getFirst());
            }
        }

        return parents;
    }

    public static String getExpressionFromGraph(VisualCpog visualCpog) {
        Collection<Node> originalSelection = null;
        ArrayList<VisualTransformableNode> groups = new ArrayList<>();
        ArrayList<Node> vertices = new ArrayList<>();
        ArrayList<String> expression = new ArrayList<>();
        String total = "";

        if (visualCpog.getSelection().isEmpty()) {
            originalSelection = visualCpog.selection();
            visualCpog.selectAll();
            if (visualCpog.selection().isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no graphs to select", "Graph to expression error",
                                JOptionPane.ERROR_MESSAGE);
                return "";
            }
        } else {
            originalSelection = copySelected(visualCpog);
        }

        groups = getScenarios(visualCpog);

        //Add vertices from group
        if (!groups.isEmpty()) {
            for (VisualTransformableNode group : groups) {
                expression.add(group.getLabel() + " =");
                originalSelection.remove(group);

                getAllGroupVertices(vertices, group);

                HashSet<Node> roots = getRoots(visualCpog, vertices);

                VisualVertex current;
                Set<Connection> totalConnections;
                ArrayList<Connection> connections = new ArrayList<>();
                HashSet<VisualVertex> visitedVertices = new HashSet<>();
                HashSet<Connection> visitedConnections = new HashSet<>();
                ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<>();

                if (roots.isEmpty()) {
                    roots.addAll(vertices);
                }

                Iterator<Node> i = roots.iterator();

                while (i.hasNext()) {
                    q.add(i.next());
                    while (!q.isEmpty()) {
                        connections.clear();
                        current = (VisualVertex) q.remove();

                        visitedVertices.add(current);

                        for (Node n : getChildren(visualCpog, current)) {
                            if (!visitedVertices.contains(n)) {
                                q.add(n);
                            }
                        }

                        totalConnections = visualCpog.getConnections(current);

                        describeArcs(expression, totalConnections, visitedVertices, visitedConnections, current, vertices, visualCpog);

                        if ((!q.isEmpty() || (i.hasNext())) && (expression.get(expression.size() - 1) != "+")) {
                            expression.add("+");
                        }

                        if ((i.hasNext()) && expression.get(expression.size() - 1) != "+") {
                            expression.add("+");
                        }
                    }

                }
                while (expression.get(expression.size() - 1) == "+") {
                    expression.remove(expression.size() - 1);
                }
                expression.add("\n");
            }
        }
        if (!originalSelection.isEmpty()) {
            vertices.clear();
            for (Node n : originalSelection) {
                if (n instanceof VisualVertex) {
                    vertices.add(n);
                } else if ((n instanceof VisualScenarioPage) || (n instanceof VisualPage)) {
                    VisualPage p = (VisualPage) n;
                    for (Node child : p.getChildren()) {
                        if (child instanceof VisualVertex) {
                            vertices.add(child);
                        }
                    }
                }
            }

            HashSet<Node> roots = getRoots(visualCpog, vertices);

            Iterator<Node> i = roots.iterator();
            VisualVertex current;
            Set<Connection> totalConnections;
            ArrayList<Connection> connections = new ArrayList<>();
            HashSet<VisualVertex> visitedVertices = new HashSet<>();
            HashSet<Connection> visitedConnections = new HashSet<>();
            ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<>();

            while (i.hasNext()) {

                q.add(i.next());
                while (!q.isEmpty()) {
                    connections.clear();
                    current = (VisualVertex) q.remove();

                    for (Node n : getChildren(visualCpog, current)) {
                        if (!visitedVertices.contains(n)) {
                            q.add(n);
                        }
                    }

                    totalConnections = visualCpog.getConnections(current);

                    describeArcs(expression, totalConnections, visitedVertices, visitedConnections, current, vertices, visualCpog);

                    if ((!q.isEmpty() || (i.hasNext())) && (expression.get(expression.size() - 1) != "+")) {
                        expression.add("+");
                    }

                    if (i.hasNext() && expression.get(expression.size() - 1) != "+") {
                        expression.add("+");
                    }
                }
            }

        }

        if (expression.get(expression.size() - 1).compareTo("+") == 0) {
            expression.remove(expression.size() - 1);
        }

        for (String ex : expression) {
            if (ex.contains("=")) {
                total = total + ex;
            } else if (ex.equals("\n")) {
                while (total.endsWith(" ") || total.endsWith("+")) {
                    total = total.substring(0, total.length() - 1);
                }
                total = total + ex;
            } else if (((ex.contains(" ")) || (ex.equals("+"))) || (!(total.contains(" " + ex + " ")) && !(total.startsWith(ex + " ")) && !(total.endsWith(" " + ex)))) {
                if (!(ex.equals("+") && total.endsWith("+"))) {
                    if ((total.endsWith("\n")) || (total.equals(""))) {
                        total = total + ex;
                    } else {
                        total = total + " " + ex;
                    }
                }
            }
        }

        if (total.endsWith("+")) total = total.substring(0, total.length() - 1);
        total = total.trim();

        return total;

    }

    public static void describeArcs(ArrayList<String> expression, Set<Connection> totalConnections, HashSet<VisualVertex> visitedVertices, HashSet<Connection> visitedConnections,
            VisualVertex current, ArrayList<Node> vertices, VisualCpog visualCpog) {
        ArrayList<Connection> connections = new ArrayList<>();
        for (Connection c : totalConnections) {
            if ((!visitedConnections.contains(c)) && (!c.getSecond().equals(current)) && (vertices.contains(c.getSecond()))) {
                connections.add(c);
            }
        }
        if (connections.size() == 1) {
            VisualArc arc = (VisualArc) connections.get(0);
            String insert = "";

            if (!(formulaToString(arc.getCondition()).equals("1"))) {
                insert = "[" + formulaToString(arc.getCondition()) + "](";
            }

            if (!(formulaToString(current.getCondition()).equals("1")) || formulaToString(current.getCondition()).compareTo(formulaToString(arc.getCondition())) != 0) {
                insert = insert + "[" + formulaToString(current.getCondition()) + "]";
            }

            insert = insert + current.getLabel() + " -> ";
            VisualVertex child = (VisualVertex) arc.getSecond();

            if (!(formulaToString(child.getCondition()).equals("1")) || !(formulaToString(child.getCondition()).equals(formulaToString(arc.getCondition())))) {
                insert = insert + "[" + formulaToString(child.getCondition()) + "]";
            }

            insert = insert + child.getLabel();
            visitedConnections.add(arc);

            HashSet<VisualArc> localVisitedArcs = new HashSet<>();
            localVisitedArcs.add(arc);

            boolean finished = false;
            while (!finished) {
                if (getChildren(visualCpog, child).size() == 1) {

                    ArrayList<Node> nextVertices = getChildren(visualCpog, child);
                    VisualVertex nextVertex = (VisualVertex) nextVertices.get(0);
                    VisualArc nextArc = (VisualArc) visualCpog.getConnection(child, nextVertex);

                    if (!localVisitedArcs.contains(nextArc)) {

                        if (formulaToString(nextArc.getCondition()).equals(formulaToString(arc.getCondition()))) {
                            insert = insert + " -> ";
                            if (!(formulaToString(nextVertex.getCondition()).equals("1")) || !(formulaToString(child.getCondition()).equals(formulaToString(arc.getCondition())))) {
                                insert = insert + "[" + formulaToString(child.getCondition()) + "]";
                            }
                            insert = insert + nextVertex.getLabel();
                            visitedConnections.add(nextArc);
                            localVisitedArcs.add(nextArc);
                            child = nextVertex;

                        }
                    } else {
                        finished = true;
                    }
                } else {
                    finished = true;
                }
            }

            if (!(formulaToString(arc.getCondition()).equals("1"))) {
                insert = insert + ")";
            }
            expression.add(insert);
        } else if (connections.size() > 1) {

            while (!connections.isEmpty()) {
                VisualArc arc = (VisualArc) connections.get(0);
                String insert = "";

                if (!formulaToString(arc.getCondition()).equals("1")) {
                    insert = "[" + formulaToString(arc.getCondition()) + "](";
                }

                if (!(formulaToString(current.getCondition()).equals("1")) || !(formulaToString(current.getCondition()).equals(formulaToString(arc.getCondition())))) {
                    insert = insert + "[" + formulaToString(current.getCondition()) + "]";
                }

                insert = insert + current.getLabel() + " -> ";

                ArrayList<VisualArc> toBeRemoved = new ArrayList<>();

                for (Connection c : connections) {
                    VisualArc a = (VisualArc) c;

                    if (a.getCondition().equals(arc.getCondition())) {
                        toBeRemoved.add(a);
                    }
                }

                if (toBeRemoved.size() > 1) {
                    insert = insert + "("; // + arc.getSecond().getLabel() + " + ";
                }

                for (VisualArc a : toBeRemoved) {
                    insert = insert + ((VisualVertex) a.getSecond()).getLabel() + " + ";
                }

                while ((insert.endsWith(" ")) || (insert.endsWith("+"))) {
                    insert = insert.substring(0, insert.length() - 1);
                }

                if (toBeRemoved.size() > 1) {
                    insert = insert + ")";
                }

                visitedConnections.addAll(toBeRemoved);
                connections.removeAll(toBeRemoved);

                expression.add(insert);
            }

        } else {
            String insert = "";
            if (!(formulaToString(current.getCondition()).equals("1"))) {
                insert = "[" + formulaToString(current.getCondition()) + "]";
            }
            insert = insert + current.getLabel();
            expression.add(insert);
        }
    }

    private static String formulaToString(BooleanFormula condition) {
        return FormulaToString.toString(condition);
    }

    public static HashSet<Node> getRoots(VisualCpog visualCpog, ArrayList<Node> vertices) {
        HashSet<Node> roots = new HashSet<>();
        Set<Connection> arcs;
        Iterator<Connection> it;
        Connection connection;
        boolean second = false;

        //get root(s)
        for (Node v : vertices) {
            arcs = visualCpog.getConnections(v);
            it = arcs.iterator();
            //The following covers root nodes, and nodes with no connections
            while (it.hasNext()) {
                connection = it.next();
                if ((!connection.getFirst().equals(v)) && (vertices.contains(connection.getFirst()))) {
                    second = true;
                    break;
                }
            }
            if (!second) {
                roots.add(v);
            }
            second = false;
        }
        return roots;
    }

    public static void getAllGroupVertices(ArrayList<Node> vertices, VisualTransformableNode group) {
        vertices.clear();
        for (VisualComponent v : group.getComponents()) {
            if (v instanceof VisualPage) {
                vertices.addAll(getPageVertices((VisualPage) v));
            } else vertices.add(v);
        }
    }

    public static void getGroups(VisualCpog visualCpog, ArrayList<VisualTransformableNode> groups) {
        ArrayList<Node> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for (Node n : visualCpog.getSelection()) {
            if ((n instanceof VisualPage) || (n instanceof VisualGroup)) {
                if (prevSelection.contains(n)) {
                    groups.add((VisualTransformableNode) n);
                }
            }
        }

        visualCpog.select(prevSelection);
    }

    public static ArrayList<VisualTransformableNode> getScenarios(VisualCpog visualCpog) {
        ArrayList<VisualTransformableNode> scenarios = new ArrayList<>();
        TreeSet<String> nameList = new TreeSet<>();
        ArrayList<Node> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for (Node n : visualCpog.getSelection()) {
            if ((n instanceof VisualScenarioPage) || (n instanceof VisualScenario)) {
                if (prevSelection.contains(n)) {
                    scenarios.add((VisualTransformableNode) n);
                    VisualTransformableNode node = (VisualTransformableNode) n;
                    nameList.add(node.getLabel());
                }
            }
        }

        ArrayList<VisualTransformableNode> result = new ArrayList<>();

        for (String name : nameList) {
            boolean found = false;
            int i = 0;
            while (!found) {
                if (scenarios.get(i).getLabel().equals(name)) {
                    result.add(scenarios.remove(i));
                    found = true;
                }
                i++;
            }
        }

        visualCpog.select(prevSelection);
        return result;
    }

    public int findVertex(ArrayList<ArrayList<Node>> outer, Node target) {
        int index = 0;
        for (ArrayList<Node> inner : outer) {
            if (inner.contains(target)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void addNode(Node v, int index, ArrayList<ArrayList<Node>> outer) {
        int removalIndex = 0;

        removalIndex = findVertex(outer, v);
        if (removalIndex >= 0) {
            outer.get(removalIndex).remove(v);
        }
        if (outer.size() - 1 < index) {
            outer.add(new ArrayList<Node>());
        }

        outer.get(index).add(v);

    }

    public boolean[][] convertToArrayForm(Collection<VisualVertex> vertices, VisualCpog visualCpog) {

        boolean[][] c = new boolean[vertices.size()][vertices.size()];

        int i = 0, j = 0;
        for (VisualVertex n1 : vertices) {
            j = 0;
            for (VisualVertex n2 : vertices) {
                if (visualCpog.hasConnection(n1, n2)) {
                    c[i][j] = true;
                } else {
                    c[i][j] = false;
                }
                j++;
            }
            i++;
        }

        return c;
    }

    public void computeTransitiveClosure(boolean[][] c) {

        for (int j = 0; j < c.length; j++) {
            for (int i = 0; i < c.length; i++) {
                for (int k = 0; k < c.length; k++) {
                    if (c[i][j] && c[j][k]) {
                        c[i][k] = true;
                    }
                }
            }
        }

    }

    public boolean[][] findTransitives(boolean[][] c) {
        boolean[][] t = new boolean[c.length][c.length];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c.length; j++) {
                for (int k = 0; k < c.length; k++) {
                    if (c[i][j] && c[j][k]) {
                        t[i][k] = true;
                    }
                }
            }
        }
        return t;
    }

    public void convertFromArrayForm(boolean[][] t, Collection<VisualVertex> vertices, VisualCpog visualCpog) {

        int i = 0, j = 0;
        for (VisualVertex n1 : vertices) {
            j = 0;
            for (VisualVertex n2 : vertices) {
                if ((t[i][j]) && (visualCpog.hasConnection(n1, n2))) {
                    if (visualCpog.hasConnection(n1, n2)) {
                        visualCpog.remove(visualCpog.getConnection(n1, n2));
                    }
                }
                j++;
            }
            i++;
        }
    }

    public boolean hasSelfLoops(boolean[][]c) {

        for (int i = 0; i < c.length; i++) {
            if (c[i][i]) {
                return true;
            }
        }
        return false;
    }

    public String replaceReferences(String text) {
        usedReferences = new ArrayList<>();
        boolean added;
        for (String k : refMap.keySet()) {
            added = false;
            if (text.contains(" " + k + " ")) {
                if (k.startsWith("[")) {
                    text = text.replaceAll(" " + k + " ", " (" + refMap.get(k).getNormalForm() + ") ");
                    added = true;
                } else {
                    text = text.replaceAll(" " + k + " ", " (" + refMap.get(k).getNormalForm() + ") ");
                    added = true;
                }
            }
            if (text.contains("]" + k + " ")) {
                text = text.replaceAll("]" + k + " ", "](" + refMap.get(k).getNormalForm() + ") ");
                added = true;
            }
            if (text.contains("(" + k + ")")) {
                text = text.replaceAll("\\(" + k + "\\)", "\\(" + refMap.get(k).getNormalForm() + "\\)");
                added = true;
            }
            if (text.contains("(" + k + " ")) {
                text = text.replaceAll("\\(" + k + " ", "\\(\\(" + refMap.get(k).getNormalForm() + "\\) ");
                added = true;
            }
            if (text.contains(" " + k + ")")) {
                text = text.replaceAll(" " + k + "\\)", " \\(" + refMap.get(k).getNormalForm() + "\\)\\)");
                added = true;
            }
            if (text.endsWith(" " + k)) {
                text = text.replace(" " + k, " (" + refMap.get(k).getNormalForm() + ")");
                added = true;
            }
            if (text.endsWith("]" + k)) {
                text = text.replace("]" + k, "](" + refMap.get(k).getNormalForm() + ")");
                added = true;
            }
            if (text.endsWith(" " + k + ")")) {
                text = text.replace(" " + k + "\\)", " (" + refMap.get(k).getNormalForm() + "\\)\\)");
                added = true;
            }

            if (added) {
                usedReferences.add(k);
            }
        }
        return text;
    }

    public void setArcConditions(HashSet<ArcCondition> arcConditionList, VisualCpog visualCpog, HashMap<String, VisualVertex> vertexMap) {
        int index;
        for (ArcCondition a : arcConditionList) {
            if (a.getBoolForm().compareTo("") != 0) {
                index = 0;
                ArrayList<String> vertexList = a.getVertexList();
                Iterator<String> it = vertexList.iterator();
                String first, second;
                VisualArc arc;

                while (it.hasNext()) {
                    first = it.next();
                    for (int c = index + 1; c < vertexList.size(); c++) {
                        second = vertexList.get(c);

                        ArrayList<String> verts1 = new ArrayList<>();
                        ArrayList<String> verts2 = new ArrayList<>();
                        int ind = 0;
                        if (first.contains("(")) {
                            first = first.replace("(", "");
                            first = first.replace(")", "");
                            while (first.contains("+")) {
                                ind = first.indexOf("+");
                                verts1.add(first.substring(0, ind));
                                first = first.substring(ind + 1);
                            }
                            verts1.add(first);
                        }
                        verts1.add(first);
                        if (second.contains("(")) {
                            second = second.replace("(", "");
                            second = second.replace(")", "");
                            while (second.contains("+")) {
                                ind = second.indexOf("+");
                                verts2.add(second.substring(0, ind));
                                second = second.substring(ind + 1);
                            }
                        }
                        verts2.add(second);

                        for (String vert1 : verts1) {
                            for (String vert2 : verts2) {
                                arc = (VisualArc) visualCpog.getConnection(vertexMap.get(vert1), vertexMap.get(vert2));
                                ArrayList<VisualArc> dupArcs = new ArrayList<>();
                                if (arc != null) {
                                    for (Connection con : visualCpog.getConnections(vertexMap.get(vert1))) {
                                        if (con.getSecond().equals(vertexMap.get(vert2))) {
                                            dupArcs.add((VisualArc) con);
                                        }
                                    }
                                    boolean conditionFound = false;
                                    ArrayList<VisualArc> toBeRemoved = new ArrayList<>();
                                    if (dupArcs.size() > 1) {
                                        for (VisualArc va : dupArcs) {
                                            if (FormulaToString.toString(va.getCondition()).compareTo("1") != 0) {
                                                toBeRemoved.add(va);
                                                conditionFound = true;
                                            }
                                        }
                                        for (VisualArc va : toBeRemoved) {
                                            dupArcs.remove(va);
                                        }

                                        if (!conditionFound && (dupArcs.size() > 1)) {
                                            for (int i = 1; i < dupArcs.size(); i++) {
                                                visualCpog.remove(dupArcs.get(i));
                                            }
                                        } else {
                                            for (int i = 0; i < dupArcs.size(); i++) {
                                                visualCpog.remove(dupArcs.get(i));
                                            }
                                        }
                                    }

                                    try {
                                        if (FormulaToString.toString(arc.getCondition()).compareTo("1") == 0) {
                                            arc.setCondition(parseBool(a.getBoolForm(), visualCpog));
                                        } else {
                                            arc.setCondition(parseBool(FormulaToString.toString(arc.getCondition()) + "|" + a.getBoolForm(), visualCpog));
                                        }
                                    } catch (ParseException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                    }
                    index++;
                }
            }
        }
    }

    public Point2D.Double getLowestVertex(VisualCpog visualCpog) {
        Collection<VisualVertex> vertices = visualCpog.getVertices(visualCpog.getCurrentLevel());
        vertices.removeAll(visualCpog.getSelection());

        ArrayList<Node> prevSelection = new ArrayList<>();
        for (Node n : visualCpog.getSelection()) prevSelection.add(n);

        ArrayList<VisualScenarioPage> pages = new ArrayList<>();
        visualCpog.selectAll();
        for (Node n : visualCpog.getSelection()) {
            if (n instanceof VisualScenarioPage) {
                pages.add((VisualScenarioPage) n);
            }
        }

        visualCpog.select(prevSelection);

        pages.removeAll(visualCpog.getSelection());

        Point2D.Double centre, startPoint = null;

        for (VisualVertex vertex : vertices) {
            centre = (Double) vertex.getCenter();
            if (startPoint == null) {
                startPoint = new Point2D.Double(centre.getX(), centre.getY());
            } else {
                if (centre.getY() > startPoint.getY()) {
                    startPoint.setLocation(startPoint.getX(), centre.getY());
                }
                if (centre.getX() < startPoint.getX()) {
                    startPoint.setLocation(centre.getX(), startPoint.getY());
                }
            }
        }
        for (VisualScenarioPage page : pages) {
            Rectangle2D.Double rect = (Rectangle2D.Double) page.getBoundingBox();
            Point2D.Double bl = new Point2D.Double(0, rect.getCenterY() + (rect.getHeight() / 2));

            if (startPoint == null) {
                startPoint = new Point2D.Double(bl.getX(), bl.getY());
            } else {
                if (bl.getY() > startPoint.getY()) {
                    startPoint.setLocation(startPoint.getX(), bl.getY());
                }
            }
        }
        if (startPoint == null) {
            startPoint = new Point2D.Double(0, 0);
        } else {
            startPoint.setLocation(startPoint.getX(), startPoint.getY());
        }

        return startPoint;

    }

    public ArrayList<String> getUsedReferences() {
        return usedReferences;
    }

    public static ArrayList<VisualComponent> getPageVertices(VisualPage p) {
        ArrayList<VisualComponent> result = new ArrayList<>();

        for (VisualComponent c : p.getComponents()) {
            if (c instanceof VisualPage) {
                result.addAll(getPageVertices((VisualPage) c));
            } else {
                result.add(c);
            }
        }
        return result;
    }

    public static ArrayList<Node> copySelected(VisualCpog visualCpog) {
        ArrayList<Node> result = new ArrayList<>();
        for (Node n : visualCpog.getSelection()) {
            result.add(n);
        }

        return result;
    }

    public static HashSet<VisualArc> getAllArcs(Container root, VisualCpog visualCpog) {
        HashSet<VisualArc> result = new HashSet<>();
        for (Node node : root.getChildren()) {
            if ((node instanceof VisualPage) || (node instanceof VisualScenarioPage)) {
                result.addAll(getAllArcs((VisualPage) node, visualCpog));
            } else if (node instanceof VisualScenario) {
                result.addAll(getAllArcs((VisualScenario) node, visualCpog));
            } else if (node instanceof VisualArc) {
                result.add((VisualArc) node);
            }
        }

        return result;
    }

    public static boolean hasEnoughScenarios(WorkspaceEntry we) {
        VisualCpog cpog = (VisualCpog) (we.getModelEntry().getVisualModel());
        return getScenarios(cpog).size() > 1;
    }

    public static boolean hasTooScenarios(WorkspaceEntry we) {
        DesktopApi.OsType os = DesktopApi.getOs();
        VisualCpog cpog = (VisualCpog) (we.getModelEntry().getVisualModel());
        if (os.isLinux()) {
            return getScenarios(cpog).size() > MAX_SCENARIOS_LINUX;
        } else {
            return getScenarios(cpog).size() > MAX_SCENARIOS_OTHER_OS;
        }
    }

    public boolean[][] copyArray(boolean[][] c) {
        boolean[][] t = new boolean[c.length][c.length];
        for (int i = 0; i < c.length; i++) {
            for (int j = 0; j < c.length; j++) {
                t[i][j] = c[i][j];
            }
        }
        return t;
    }

}
