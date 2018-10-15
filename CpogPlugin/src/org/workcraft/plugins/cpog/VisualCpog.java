package org.workcraft.plugins.cpog;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CommentGeneratorTool;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.cpog.observers.VariableConsistencySupervisor;
import org.workcraft.plugins.cpog.properties.BooleanFormulaPropertyDescriptor;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.util.Hierarchy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@DisplayName("Conditional Partial Order Graph")
public class VisualCpog extends AbstractVisualModel {

    public VisualCpog(Cpog model) {
        this(model, null);
    }

    public VisualCpog(Cpog model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
        new VariableConsistencySupervisor(this).attach(getRoot());
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new CpogSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new ConnectionTool(false, true, true));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(Vertex.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(Variable.class)));
        tools.add(new NodeGeneratorTool(new DefaultNodeGenerator(RhoClause.class)));
        setGraphEditorTools(tools);
    }

    @Override
    public Cpog getMathModel() {
        return (Cpog) super.getMathModel();
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first instanceof VisualVariable && !getPreset(first).isEmpty()) {
            throw new InvalidConnectionException("Variables do not support multiple connections.");
        }
        if (second instanceof VisualVariable && !getPreset(second).isEmpty()) {
            throw new InvalidConnectionException("Variables do not support multiple connections.");
        }

        if ((first instanceof VisualVertex) && (second instanceof VisualVertex)) return;
        if ((first instanceof VisualVertex) && (second instanceof VisualVariable)) return;
        if ((first instanceof VisualVariable) && (second instanceof VisualVertex)) return;

        throw new InvalidConnectionException("Invalid connection.");
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);
        VisualConnection ret = null;
        if ((first instanceof VisualVertex) && (second instanceof VisualVertex)) {
            VisualVertex v = (VisualVertex) first;
            VisualVertex u = (VisualVertex) second;
            ret = connect(v, u);
        } else {
            VisualVertex v;
            VisualVariable u;
            if (first instanceof VisualVertex) {
                v = (VisualVertex) first;
                u = (VisualVariable) second;
            } else {
                v = (VisualVertex) second;
                u = (VisualVariable) first;
            }
            if (mConnection == null) {
                mConnection = getMathModel().connect(v.getMathVertex(), u.getMathVariable());
            }
            ret = new VisualDynamicVariableConnection((DynamicVariableConnection) mConnection, v, u);
            Hierarchy.getNearestContainer(v, u).add(ret);
        }
        return ret;
    }

    public VisualArc connect(VisualVertex v, VisualVertex u) {
        Arc con = getMathModel().connect(v.getMathVertex(), u.getMathVertex());
        VisualArc arc = new VisualArc(con, v, u);
        Hierarchy.getNearestContainer(v, u).add(arc);
        return arc;
    }

    @Override
    public boolean isGroupable(Node node) {
        return (node instanceof VisualVertex) || (node instanceof VisualVariable);
    }

    @Override
    public VisualGroup groupSelection() {
        return groupSelection(null);
    }

    public VisualScenario groupSelection(String graphName) {
        VisualScenario scenario = null;
        Collection<Node> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
        if (nodes.size() >= 1) {
            scenario = new VisualScenario();
            if (graphName != null) {
                scenario.setLabel(graphName);
            }
            getCurrentLevel().add(scenario);
            getCurrentLevel().reparent(nodes, scenario);
            Point2D centre = TransformHelper.getSnappedCentre(nodes);
            VisualModelTransformer.translateNodes(nodes, -centre.getX(), -centre.getY());
            scenario.setPosition(centre);
            select(scenario);
        }
        return scenario;
    }

    // TODO: Add safe versions of these methods; see getVertices(Container root).
    @Deprecated
    public Collection<VisualScenario> getGroups() {
        return Hierarchy.getChildrenOfType(getRoot(), VisualScenario.class);
    }

    @Deprecated
    public Collection<VisualVariable> getVariables() {
        return Hierarchy.getChildrenOfType(getRoot(), VisualVariable.class);
    }

    @Deprecated
    public Collection<VisualVertex> getVertices() {
        return Hierarchy.getChildrenOfType(getRoot(), VisualVertex.class);
    }

    public Collection<VisualVertex> getVertices(Container root) {
        return Hierarchy.getChildrenOfType(root, VisualVertex.class);
    }

    public Collection<VisualVariable> getVariables(Container root) {
        return Hierarchy.getChildrenOfType(root, VisualVariable.class);
    }

    public Collection<VisualArc> getArcs(Container root) {
        return Hierarchy.getChildrenOfType(root, VisualArc.class);
    }

    public VisualVertex createVisualVertex(Container container) {
        Vertex mathVertex = new Vertex();
        getMathModel().add(mathVertex);

        VisualVertex vertex = new VisualVertex(mathVertex);
        container.add(vertex);
        return vertex;
    }

    public VisualVariable createVisualVariable() {
        Variable mathVariable = new Variable();
        getMathModel().add(mathVariable);

        VisualVariable variable = new VisualVariable(mathVariable);

        getRoot().add(variable);

        return variable;
    }

    public VisualScenario createVisualScenario() {
        VisualScenario scenario = new VisualScenario();
        getRoot().add(scenario);
        return scenario;
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if ((node instanceof VisualRhoClause) || (node instanceof VisualVertex) || (node instanceof VisualArc)) {
            properties.add(new BooleanFormulaPropertyDescriptor(getMathModel(), node));
        }
        return properties;
    }

    public void removeWithoutNotify(Node node) {
        if (node.getParent() instanceof VisualPage) {
            ((VisualPage) node.getParent()).removeWithoutNotify(node);
        } else if (node.getParent() instanceof VisualGroup) {
            ((VisualGroup) node.getParent()).removeWithoutNotify(node);
        }
    }

    public VisualScenarioPage groupScenarioPageSelection(String graphName) {
        VisualScenarioPage scenario = null;
        PageNode pageNode = new PageNode();
        Collection<Node> nodes = SelectionHelper.getGroupableCurrentLevelSelection(this);
        if (nodes.size() >= 1) {
            scenario = new VisualScenarioPage(pageNode);
            if (graphName != null) {
                scenario.setLabel(graphName);
            }
            getCurrentLevel().add(scenario);
            getCurrentLevel().reparent(nodes, scenario);
            Point2D centre = TransformHelper.getSnappedCentre(nodes);
            VisualModelTransformer.translateNodes(nodes, -centre.getX(), -centre.getY());
            scenario.setPosition(centre);
            select(scenario);
        }
        return scenario;
    }

}
