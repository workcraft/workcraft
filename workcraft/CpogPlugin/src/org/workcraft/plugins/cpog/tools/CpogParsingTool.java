package org.workcraft.plugins.cpog.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.cpog.*;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CpogParsingTool {

    private static final int MAX_SCENARIOS_LINUX = 680;
    private static final int MAX_SCENARIOS_OTHER_OS = 340;

    private final HashMap<String, Variable> variableMap;
    private int xpos;
    private final HashMap<String, GraphReference> refMap;
    private ArrayList<String> usedReferences;

    public CpogParsingTool(HashMap<String, Variable> variableMap, int xpos, HashMap<String, GraphReference> refMap) {
        this.variableMap = variableMap;
        this.xpos = xpos;
        this.refMap = refMap;
    }

    public BooleanFormula parseBool(String bool, final VisualCpog visualCpog) throws ParseException {
        return BooleanFormulaParser.parse(bool, label -> labelToVar(label, visualCpog));
    }

    private BooleanVariable labelToVar(String label, final VisualCpog visualCpog) {
        if (variableMap.containsKey(label)) {
            if (variableMap.get(label).getParent() != null) {
                return variableMap.get(label);
            }
            variableMap.remove(label);
        }

        VisualVariable visVar = visualCpog.createVisualVariable();
        visVar.setLabel(label);
        visVar.setPosition(new Point2D.Double(xpos, -2));
        xpos++;
        variableMap.put(label, visVar.getReferencedComponent());

        return variableMap.get(label);
    }

    public void bfsLayout(Queue<VisualNode> q, VisualCpog visualCpog, double originalX, double originalY) {
        ArrayList<ArrayList<VisualNode>> outer = new ArrayList<>();
        HashSet<VisualPage> pages = new HashSet<>();
        VisualNode current = q.remove();
        ArrayList<VisualNode> children = getChildren(visualCpog, current);

        outer.add(new ArrayList<>());
        outer.get(0).add(current);
        outer.add(new ArrayList<>());

        for (VisualNode child : children) {
            q.add(child);
            outer.get(1).add(child);
        }

        findAllChildren(q, visualCpog, outer, pages);
        positionNodes(originalX, originalY, outer);
    }

    public double positionNodes(double originalX, double originalY, ArrayList<ArrayList<VisualNode>> outer) {
        Point2D centre = new Point2D.Double(0.0, originalY);

        double x = originalX;
        double y = 0;

        Iterator<ArrayList<VisualNode>> it = outer.iterator();

        while (it.hasNext()) {
            ArrayList<VisualNode> inner = it.next();
            if (inner.size() > 1) {
                y = centre.getY() - (inner.size() / 2.0);
            } else {
                y = centre.getY();
            }
            for (VisualNode n : inner) {
                if (n instanceof VisualVertex v) {
                    if ((v.getParent() instanceof VisualPage p) && (refMap.containsKey(((VisualPage) v.getParent()).getLabel()))) {
                        Point2D.Double newPosition = new
                                Point2D.Double(refMap.get(p.getLabel()).getVertMap().get(v.getLabel()).getX(),
                                        refMap.get(p.getLabel()).getVertMap().get(v.getLabel()).getY());
                        v.setPosition(newPosition);
                    } else {
                        v.setPosition(new Point2D.Double(x, y));
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

    public void findAllChildren(Queue<VisualNode> q, VisualCpog visualCpog,
            ArrayList<ArrayList<VisualNode>> outer, HashSet<VisualPage> pages) {
        VisualNode current;
        ArrayList<VisualNode> children;
        int index = 0;
        HashSet<VisualNode> visitedNodes = new HashSet<>();
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
                for (VisualNode child : children) {
                    q.add(child);
                    addNode(child, index + 1, outer);
                }
            }
        }
    }

    public static ArrayList<VisualNode> getChildren(VisualCpog visualCpog, VisualNode node) {
        ArrayList<VisualNode> children = new ArrayList<>();
        HashSet<VisualArc> arcs = getAllArcs(visualCpog.getRoot(), visualCpog);

        for (VisualArc arc : arcs) {
            if (arc.getFirst().equals(node)) {
                children.add(arc.getSecond());
            }
        }

        return children;
    }

    public static HashSet<VisualNode> getParents(VisualCpog visualCpog, VisualNode node) {
        HashSet<VisualNode> parents = new HashSet<>();
        HashSet<VisualArc> arcs = getAllArcs(visualCpog.getRoot(), visualCpog);

        for (VisualArc arc : arcs) {
            if (arc.getSecond().equals(node)) {
                parents.add(arc.getFirst());
            }
        }

        return parents;
    }

    public static String getExpressionFromGraph(VisualCpog visualCpog) {
        Collection<VisualNode> originalSelection = null;
        if (visualCpog.getSelection().isEmpty()) {
            originalSelection = visualCpog.getSelection();
            visualCpog.selectAll();
            if (visualCpog.getSelection().isEmpty()) {
                DialogUtils.showError("There are no graphs to select");
                return "";
            }
        } else {
            originalSelection = copySelected(visualCpog);
        }

        ArrayList<VisualTransformableNode> groups = getScenarios(visualCpog);
        ArrayList<VisualNode> vertices = new ArrayList<>();
        ArrayList<String> expression = new ArrayList<>();
        StringBuilder total = new StringBuilder();

        // Add vertices from group
        if (!groups.isEmpty()) {
            for (VisualTransformableNode group : groups) {
                expression.add(group.getLabel() + " =");
                originalSelection.remove(group);

                getAllGroupVertices(vertices, group);

                HashSet<VisualNode> roots = getRoots(visualCpog, vertices);

                VisualVertex current;
                Set<VisualConnection> totalConnections;
                ArrayList<VisualConnection> connections = new ArrayList<>();
                HashSet<VisualVertex> visitedVertices = new HashSet<>();
                HashSet<VisualConnection> visitedConnections = new HashSet<>();
                ConcurrentLinkedQueue<VisualNode> q = new ConcurrentLinkedQueue<>();

                if (roots.isEmpty()) {
                    roots.addAll(vertices);
                }

                Iterator<VisualNode> i = roots.iterator();

                while (i.hasNext()) {
                    q.add(i.next());
                    while (!q.isEmpty()) {
                        connections.clear();
                        current = (VisualVertex) q.remove();

                        visitedVertices.add(current);

                        for (VisualNode n : getChildren(visualCpog, current)) {
                            if (!visitedVertices.contains(n)) {
                                q.add(n);
                            }
                        }

                        totalConnections = visualCpog.getConnections(current);

                        describeArcs(expression, totalConnections, visitedConnections, current, vertices, visualCpog);

                        if ((!q.isEmpty() || i.hasNext()) && !"+".equals(expression.get(expression.size() - 1))) {
                            expression.add("+");
                        }

                        if (i.hasNext() && !"+".equals(expression.get(expression.size() - 1))) {
                            expression.add("+");
                        }
                    }

                }
                while ("+".equals(expression.get(expression.size() - 1))) {
                    expression.remove(expression.size() - 1);
                }
                expression.add("\n");
            }
        }
        if (!originalSelection.isEmpty()) {
            vertices.clear();
            for (VisualNode n : originalSelection) {
                if (n instanceof VisualVertex) {
                    vertices.add(n);
                } else if ((n instanceof VisualPage p)) {
                    for (Node child : p.getChildren()) {
                        if (child instanceof VisualVertex) {
                            vertices.add((VisualVertex) child);
                        }
                    }
                }
            }

            HashSet<VisualNode> roots = getRoots(visualCpog, vertices);

            Iterator<VisualNode> i = roots.iterator();
            VisualVertex current;
            Set<VisualConnection> totalConnections;
            ArrayList<VisualConnection> connections = new ArrayList<>();
            HashSet<VisualVertex> visitedVertices = new HashSet<>();
            HashSet<VisualConnection> visitedConnections = new HashSet<>();
            ConcurrentLinkedQueue<VisualNode> q = new ConcurrentLinkedQueue<>();

            while (i.hasNext()) {

                q.add(i.next());
                while (!q.isEmpty()) {
                    connections.clear();
                    current = (VisualVertex) q.remove();

                    for (VisualNode n : getChildren(visualCpog, current)) {
                        if (!visitedVertices.contains(n)) {
                            q.add(n);
                        }
                    }

                    totalConnections = visualCpog.getConnections(current);

                    describeArcs(expression, totalConnections, visitedConnections, current, vertices, visualCpog);

                    if ((!q.isEmpty() || i.hasNext()) && !"+".equals(expression.get(expression.size() - 1))) {
                        expression.add("+");
                    }

                    if (i.hasNext() && !"+".equals(expression.get(expression.size() - 1))) {
                        expression.add("+");
                    }
                }
            }

        }

        if ("+".equals(expression.get(expression.size() - 1))) {
            expression.remove(expression.size() - 1);
        }

        for (String ex : expression) {
            if (ex.contains("=")) {
                total.append(ex);
            } else if ("\n".equals(ex)) {
                while (total.toString().endsWith(" ") || total.toString().endsWith("+")) {
                    total = new StringBuilder(total.substring(0, total.length() - 1));
                }
                total.append(ex);
            } else if ((ex.contains(" ") || "+".equals(ex))
                    || (!total.toString().contains(" " + ex + " ")
                    && !total.toString().startsWith(ex + " ")
                    && !total.toString().endsWith(" " + ex))) {

                if (!("+".equals(ex) && total.toString().endsWith("+"))) {
                    if (total.toString().endsWith("\n") || total.isEmpty()) {
                        total.append(ex);
                    } else {
                        total.append(' ');
                        total.append(ex);
                    }
                }
            }
        }

        if (total.toString().endsWith("+")) {
            total = new StringBuilder(total.substring(0, total.length() - 1));
        }
        total = new StringBuilder(total.toString().trim());

        return total.toString();

    }

    public static void describeArcs(ArrayList<String> expression, Set<VisualConnection> totalConnections,
            HashSet<VisualConnection> visitedConnections, VisualVertex current,
            ArrayList<VisualNode> vertices, VisualCpog visualCpog) {

        ArrayList<VisualConnection> connections = new ArrayList<>();
        for (VisualConnection c : totalConnections) {
            if ((!visitedConnections.contains(c)) && (!c.getSecond().equals(current)) && (vertices.contains(c.getSecond()))) {
                connections.add(c);
            }
        }
        String currentCondition = formulaToString(current.getCondition());
        if (connections.size() == 1) {
            VisualArc arc = (VisualArc) connections.get(0);
            String insert = "";

            String arcCondition = formulaToString(arc.getCondition());
            if (!"1".equals(arcCondition)) {
                insert = "[" + arcCondition + "](";
            }

            if (!"1".equals(currentCondition) || currentCondition.compareTo(arcCondition) != 0) {
                insert += "[" + currentCondition + "]";
            }

            insert += current.getLabel() + " -> ";
            VisualVertex child = (VisualVertex) arc.getSecond();

            if (!"1".equals(formulaToString(child.getCondition())) || !formulaToString(child.getCondition()).equals(arcCondition)) {
                insert += "[" + formulaToString(child.getCondition()) + "]";
            }

            insert += child.getLabel();
            visitedConnections.add(arc);

            HashSet<VisualArc> localVisitedArcs = new HashSet<>();
            localVisitedArcs.add(arc);

            boolean finished = false;
            while (!finished) {
                if (getChildren(visualCpog, child).size() == 1) {

                    ArrayList<VisualNode> nextVertices = getChildren(visualCpog, child);
                    VisualVertex nextVertex = (VisualVertex) nextVertices.get(0);
                    VisualArc nextArc = (VisualArc) visualCpog.getConnection(child, nextVertex);

                    if (!localVisitedArcs.contains(nextArc)) {

                        if (formulaToString(nextArc.getCondition()).equals(arcCondition)) {
                            insert += " -> ";
                            String nextVertexCondition = formulaToString(nextVertex.getCondition());
                            String childCondition = formulaToString(child.getCondition());
                            if (!"1".equals(nextVertexCondition) || !childCondition.equals(arcCondition)) {
                                insert += "[" + formulaToString(child.getCondition()) + "]";
                            }
                            insert += nextVertex.getLabel();
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

            if (!"1".equals(arcCondition)) {
                insert += ")";
            }
            expression.add(insert);
        } else if (connections.size() > 1) {

            while (!connections.isEmpty()) {
                VisualArc arc = (VisualArc) connections.get(0);
                String insert = "";

                String arcCondition = formulaToString(arc.getCondition());
                if (!"1".equals(arcCondition)) {
                    insert = "[" + arcCondition + "](";
                }

                if (!"1".equals(currentCondition) || !currentCondition.equals(arcCondition)) {
                    insert += "[" + currentCondition + "]";
                }

                insert += current.getLabel() + " -> ";

                ArrayList<VisualArc> toBeRemoved = new ArrayList<>();

                for (VisualConnection c : connections) {
                    VisualArc a = (VisualArc) c;

                    if (a.getCondition().equals(arc.getCondition())) {
                        toBeRemoved.add(a);
                    }
                }

                if (toBeRemoved.size() > 1) {
                    insert += '(';
                }

                for (VisualArc a : toBeRemoved) {
                    insert += ((VisualVertex) a.getSecond()).getLabel() + " + ";
                }

                while ((insert.endsWith(" ")) || (insert.endsWith("+"))) {
                    insert = insert.substring(0, insert.length() - 1);
                }

                if (toBeRemoved.size() > 1) {
                    insert += ')';
                }

                visitedConnections.addAll(toBeRemoved);
                connections.removeAll(toBeRemoved);

                expression.add(insert);
            }

        } else {
            String insert = "";
            if (!"1".equals(currentCondition)) {
                insert = '[' + currentCondition + ']';
            }
            insert += current.getLabel();
            expression.add(insert);
        }
    }

    private static String formulaToString(BooleanFormula condition) {
        return StringGenerator.toString(condition);
    }

    public static HashSet<VisualNode> getRoots(VisualCpog visualCpog, ArrayList<VisualNode> vertices) {
        HashSet<VisualNode> roots = new HashSet<>();
        Set<VisualConnection> arcs;
        Iterator<VisualConnection> it;
        VisualConnection connection;
        boolean second = false;

        // Get root(s)
        for (VisualNode v : vertices) {
            arcs = visualCpog.getConnections(v);
            it = arcs.iterator();
            // The following covers root nodes, and nodes with no connections
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

    public static void getAllGroupVertices(ArrayList<VisualNode> vertices, VisualTransformableNode group) {
        vertices.clear();
        for (VisualComponent v : group.getComponents()) {
            if (v instanceof VisualPage) {
                vertices.addAll(getPageVertices((VisualPage) v));
            } else vertices.add(v);
        }
    }

    public static void getGroups(VisualCpog visualCpog, ArrayList<VisualTransformableNode> groups) {
        ArrayList<VisualNode> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for (VisualNode n : visualCpog.getSelection()) {
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
        ArrayList<VisualNode> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for (VisualNode n : visualCpog.getSelection()) {
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

    public int findVertex(ArrayList<ArrayList<VisualNode>> outer, VisualNode target) {
        int index = 0;
        for (ArrayList<VisualNode> inner : outer) {
            if (inner.contains(target)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public void addNode(VisualNode v, int index, ArrayList<ArrayList<VisualNode>> outer) {
        int removalIndex = 0;

        removalIndex = findVertex(outer, v);
        if (removalIndex >= 0) {
            outer.get(removalIndex).remove(v);
        }
        if (outer.size() - 1 < index) {
            outer.add(new ArrayList<>());
        }

        outer.get(index).add(v);

    }

    public boolean[][] convertToArrayForm(Collection<VisualVertex> vertices, VisualCpog visualCpog) {
        boolean[][] c = new boolean[vertices.size()][vertices.size()];
        int i = 0;
        for (VisualVertex n1 : vertices) {
            int j = 0;
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
        int i = 0;
        for (VisualVertex n1 : vertices) {
            int j = 0;
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
            if (text.contains(' ' + k + ' ')) {
                text = text.replace(' ' + k + ' ', " (" + refMap.get(k).getNormalForm() + ") ");
                added = true;
            }
            if (text.contains(']' + k + ' ')) {
                text = text.replace(']' + k + ' ', "](" + refMap.get(k).getNormalForm() + ") ");
                added = true;
            }
            if (text.contains('(' + k + ')')) {
                text = text.replace('(' + k + ')', "(" + refMap.get(k).getNormalForm() + ")");
                added = true;
            }
            if (text.contains('(' + k + ' ')) {
                text = text.replace('(' + k + ' ', "((" + refMap.get(k).getNormalForm() + ") ");
                added = true;
            }
            if (text.contains(' ' + k + ')')) {
                text = text.replace(' ' + k + ')', " (" + refMap.get(k).getNormalForm() + "))");
                added = true;
            }
            if (text.endsWith(' ' + k)) {
                text = text.replace(' ' + k, " (" + refMap.get(k).getNormalForm() + ")");
                added = true;
            }
            if (text.endsWith(']' + k)) {
                text = text.replace(']' + k, "](" + refMap.get(k).getNormalForm() + ")");
                added = true;
            }
            if (text.endsWith(' ' + k + ')')) {
                text = text.replace(' ' + k + ')', " (" + refMap.get(k).getNormalForm() + "))");
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
            if (!a.getBoolForm().isEmpty()) {
                index = 0;
                ArrayList<String> vertexList = a.getVertexList();
                Iterator<String> it = vertexList.iterator();
                String first;
                String second;
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
                                    for (VisualConnection con : visualCpog.getConnections(vertexMap.get(vert1))) {
                                        if (con.getSecond().equals(vertexMap.get(vert2))) {
                                            dupArcs.add((VisualArc) con);
                                        }
                                    }
                                    boolean conditionFound = false;
                                    ArrayList<VisualArc> toBeRemoved = new ArrayList<>();
                                    if (dupArcs.size() > 1) {
                                        for (VisualArc va : dupArcs) {
                                            if (!"1".equals(StringGenerator.toString(va.getCondition()))) {
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
                                        if ("1".equals(StringGenerator.toString(arc.getCondition()))) {
                                            arc.setCondition(parseBool(a.getBoolForm(), visualCpog));
                                        } else {
                                            arc.setCondition(parseBool(StringGenerator.toString(arc.getCondition()) + "|" + a.getBoolForm(), visualCpog));
                                        }
                                    } catch (ParseException e) {
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

    public Point2D getLowestVertex(VisualCpog visualCpog) {
        Collection<VisualVertex> vertices = visualCpog.getVertices(visualCpog.getCurrentLevel());
        vertices.removeAll(visualCpog.getSelection());

        ArrayList<VisualNode> prevSelection = copySelected(visualCpog);

        ArrayList<VisualScenarioPage> pages = new ArrayList<>();
        visualCpog.selectAll();
        for (VisualNode n : visualCpog.getSelection()) {
            if (n instanceof VisualScenarioPage) {
                pages.add((VisualScenarioPage) n);
            }
        }

        visualCpog.select(prevSelection);

        pages.removeAll(visualCpog.getSelection());

        Point2D startPoint = null;
        for (VisualVertex vertex : vertices) {
            Point2D centre = vertex.getCenter();
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

    public static ArrayList<VisualNode> copySelected(VisualCpog visualCpog) {
        ArrayList<VisualNode> result = new ArrayList<>();
        for (VisualNode n : visualCpog.getSelection()) {
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
        VisualCpog cpog = WorkspaceUtils.getAs(we, VisualCpog.class);
        return getScenarios(cpog).size() > 1;
    }

    public static boolean hasTooScenarios(WorkspaceEntry we) {
        DesktopApi.OsType os = DesktopApi.getOs();
        VisualCpog cpog = WorkspaceUtils.getAs(we, VisualCpog.class);
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
