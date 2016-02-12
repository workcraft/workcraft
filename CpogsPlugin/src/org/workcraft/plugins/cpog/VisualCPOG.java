/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.plugins.cpog;

import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.jj.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.jj.ParseException;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.util.Hierarchy;

@DisplayName("Conditional Partial Order Graph")
@CustomTools(CustomToolsProvider.class)
public class VisualCPOG extends AbstractVisualModel {
    private final class BooleanFormulaPropertyDescriptor implements PropertyDescriptor {
        private final Node node;

        private BooleanFormulaPropertyDescriptor(Node node) {
            this.node = node;
        }

        @Override
        public Map<Object, String> getChoice() {
            return null;
        }

        @Override
        public String getName() {
            if (node instanceof VisualRhoClause) return "Function";
            return "Condition";
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public Object getValue() throws InvocationTargetException {
            if (node instanceof VisualRhoClause) return FormulaToString.toString(((VisualRhoClause)node).getFormula());
            if (node instanceof VisualVertex) return FormulaToString.toString(((VisualVertex)node).getCondition());
            return FormulaToString.toString(((VisualArc)node).getCondition());
        }

        @Override
        public void setValue(Object value) throws InvocationTargetException {
            try {
                if (node instanceof VisualRhoClause) {
                    ((VisualRhoClause)node).setFormula(BooleanParser.parse((String)value, mathModel.getVariables()));
                } else if (node instanceof VisualArc) {
                    ((VisualArc)node).setCondition(BooleanParser.parse((String)value, mathModel.getVariables()));
                } else if (node instanceof VisualVertex) {
                    ((VisualVertex)node).setCondition(BooleanParser.parse((String)value, mathModel.getVariables()));
                }
            } catch (ParseException e) {
                throw new InvocationTargetException(e);
            }
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean isCombinable() {
            return true;
        }

        @Override
        public boolean isTemplatable() {
            return true;
        }
    }

    private CPOG mathModel;
    private CpogSelectionTool selectionTool;

    public VisualCPOG(CPOG model) {
        this(model, null);
    }

    public VisualCPOG(CPOG model, VisualGroup root) {
        super(model, root);
        this.mathModel = model;

        if (root == null) {
            try {
                createDefaultFlatStructure();
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
        new ConsistencyEnforcer(this).attach(getRoot());
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Self loops are not allowed.");
        }
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
                mConnection = mathModel.connect(v.getMathVertex(), u.getMathVariable());
            }
            ret = new VisualDynamicVariableConnection((DynamicVariableConnection)mConnection, v, u);
            Hierarchy.getNearestContainer(v, u).add(ret);
        }
        return ret;
    }

    public VisualArc connect(VisualVertex v, VisualVertex u) {
        Arc con = mathModel.connect(v.getMathVertex(), u.getMathVertex());
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
        mathModel.add(mathVertex);

        VisualVertex vertex = new VisualVertex(mathVertex);
        container.add(vertex);
        return vertex;
    }

    public VisualVariable createVisualVariable() {
        Variable mathVariable = new Variable();
        mathModel.add(mathVariable);

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
        if (node != null) {
            if (node instanceof VisualRhoClause ||
                    node instanceof VisualVertex ||
                    node instanceof VisualArc) {
                properties.add(new BooleanFormulaPropertyDescriptor(node));
            }
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
