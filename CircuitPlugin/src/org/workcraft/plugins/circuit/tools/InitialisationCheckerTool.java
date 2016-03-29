package org.workcraft.plugins.circuit.tools;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

public class InitialisationCheckerTool extends VerificationTool {

    @Override
    public String getDisplayName() {
        return "Check circuit initialisation";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Circuit;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final GraphEditorPanel currentEditor = mainWindow.getEditor(we);
        final ToolboxPanel toolbox = currentEditor.getToolBox();
        final InitialisationAnalyserTool tool = toolbox.getToolInstance(InitialisationAnalyserTool.class);
        Circuit circuit = (Circuit) we.getModelEntry().getMathModel();
        setInitialisationState(tool, circuit);
        toolbox.selectTool(tool);
    }

    private void setInitialisationState(InitialisationAnalyserTool tool, Circuit circuit) {
        HashSet<Node> initHighSet = new HashSet<>();
        HashSet<Node> initLowSet = new HashSet<>();
        HashSet<Node> initErrorSet = new HashSet<>();
        Queue<Connection> queue = new LinkedList<>();
        for (FunctionContact contact: circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getInitialised()) {
                HashSet<Node> init = contact.getInitToOne() ? initHighSet : initLowSet;
                if (init.add(contact)) {
                    Set<Connection> connections = circuit.getConnections(contact);
                    queue.addAll(connections);
                }
            }
        }
        while (!queue.isEmpty()) {
            Connection connection = queue.remove();
            Node fromNode = connection.getFirst();
            HashSet<Node> nodeInitLevelSet = chooseNodeLevelSet(fromNode, initHighSet, initLowSet);
            if (nodeInitLevelSet != null) {
                if (nodeInitLevelSet.add(connection)) {
                    Node toNode = connection.getSecond();
                    if (nodeInitLevelSet.add(toNode)) {
                        Node parent = toNode.getParent();
                        if (parent instanceof FunctionComponent) {
                            LinkedList<BooleanVariable> variables = new LinkedList<>();
                            LinkedList<BooleanFormula> values = new LinkedList<>();
                            LinkedList<FunctionContact> outputPins = new LinkedList<>();
                            for (FunctionContact contact: Hierarchy.getChildrenOfType(parent, FunctionContact.class)) {
                                HashSet<Node> contactInitLevelSet = chooseNodeLevelSet(contact, initHighSet, initLowSet);
                                if (contactInitLevelSet != null) {
                                    variables.add(contact);
                                    values.add(contactInitLevelSet == initHighSet ? One.instance() : Zero.instance());
                                }
                                if (contact.isOutput()) {
                                    outputPins.add(contact);
                                }
                            }
                            for (FunctionContact outputPin: outputPins) {
                                Set<Node> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values, initHighSet, initLowSet);
                                if (outputInitLevelSet != null) {
                                    if (outputInitLevelSet.add(outputPin)) {
                                        Set<Connection> connections = circuit.getConnections(outputPin);
                                        queue.addAll(connections);
                                    }
                                }
                            }
                        } else {
                            Set<Connection> connections = circuit.getConnections(toNode);
                            queue.addAll(connections);
                        }
                    }
                }
            }
        }
        tool.setState(initHighSet, initLowSet, initErrorSet);
    }

    private HashSet<Node> chooseNodeLevelSet(Node node, HashSet<Node> highSet, HashSet<Node> lowSet) {
        if (highSet.contains(node)) {
            return highSet;
        }
        if (lowSet.contains(node)) {
            return lowSet;
        }
        return null;
    }

    private HashSet<Node> chooseFunctionLevelSet(FunctionContact contact, LinkedList<BooleanVariable> variables,
            LinkedList<BooleanFormula> values, HashSet<Node> highSet, HashSet<Node> lowSet) {
        BooleanFormula setFunction = BooleanUtils.cleverReplace(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.cleverReplace(contact.getResetFunction(), variables, values);
        if (isEvaluatedHigh(setFunction, resetFunction)) {
            return highSet;
        } else if (isEvaluatedLow(setFunction, resetFunction)) {
            return lowSet;
        }
        return null;
    }

    private boolean isEvaluatedHigh(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return One.instance().equals(setFunction) && ((resetFunction == null) || Zero.instance().equals(resetFunction));
    }

    private boolean isEvaluatedLow(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return Zero.instance().equals(setFunction) && ((resetFunction == null) || One.instance().equals(resetFunction));
    }

}
