package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Not;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.formula.utils.FormulaToLiterals;
import org.workcraft.formula.utils.FormulaToString;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.naryformula.SplitForm;
import org.workcraft.plugins.circuit.naryformula.SplitFormGenerator;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.util.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class SplitGateTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private class NodeConnectionPair extends Pair<VisualNode, VisualConnection> {
        NodeConnectionPair(VisualNode first, VisualConnection second) {
            super(first, second);
        }
    }

    @Override
    public String getDisplayName() {
        return "Split multi-level gates (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Split multi-level gate";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        return (node instanceof VisualFunctionComponent) && ((VisualFunctionComponent) node).isGate();
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> components = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            components.addAll(Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionComponent.class));
            Collection<Node> selection = circuit.getSelection();
            if (!selection.isEmpty()) {
                components.retainAll(selection);
            }
        }
        return components;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualFunctionComponent)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualFunctionComponent component = (VisualFunctionComponent) node;
            if (component.isGate()) {
                transformGate(circuit, component);
            }
        }
    }

    private void transformGate(VisualCircuit circuit, VisualFunctionComponent bigGate) {
        VisualFunctionContact bigOutputContact = bigGate.getGateOutput();

        BooleanFormula setFunction = bigOutputContact.getSetFunction();
        String str = gateToString(circuit, bigGate);
        if (setFunction == null) {
            LogUtils.logWarning("Gate " + str + " cannot be split as it does not have set functions defined");
            return;
        }

        BooleanFormula resetFunction = bigOutputContact.getResetFunction();
        if ((setFunction != null) && (resetFunction != null)) {
            LogUtils.logWarning("Gate " + str + " cannot be split as it has both set and reset functions defined");
            return;
        }

        SplitForm functions = SplitFormGenerator.generate(setFunction);
        if (getSplitGateCount(functions) < 2) {
            LogUtils.logWarning("Gate " + str + " cannot be split as it is too simple");
            return;
        }

        LogUtils.logInfo("Splitting multi-level gate " + str + "into:");
        List<NodeConnectionPair> fromNodeConnections = getComponentDriverNodes(circuit, bigGate);
        Set<NodeConnectionPair> toNodeConnections = getComponentNonLoopDrivenNodes(circuit, bigGate);
        Container container = (Container) bigGate.getParent();
        circuit.remove(bigGate);

        Stack<Set<NodeConnectionPair>> toNodeConnectionsStack = new Stack<>();
        toNodeConnectionsStack.push(toNodeConnections);

        Iterator<NodeConnectionPair> fromNodeConnectionIterator = fromNodeConnections.iterator();
        boolean isRootGate = true;
        LinkedList<VisualFunctionComponent> nonRootGates = new LinkedList<>();
        for (BooleanFormula function: functions.getClauses()) {
            if (function instanceof BooleanVariable) {
                connectTerminal(circuit, fromNodeConnectionIterator, toNodeConnectionsStack);
            } else {
                VisualFunctionComponent gate = insertGate(circuit, function, container, toNodeConnectionsStack);
                if (!isRootGate) {
                    nonRootGates.push(gate);
                } else {
                    Point2D pos = new Point2D.Double(bigGate.getX() + 1.0, bigGate.getY());
                    gate.setPosition(pos);
                    VisualFunctionContact outputContact = gate.getGateOutput();
                    // Update fromNodes for self-loops
                    fromNodeConnections.replaceAll(pair -> (pair.getFirst() == bigOutputContact)
                            ? new NodeConnectionPair(outputContact, pair.getSecond()) : pair);
                    outputContact.copyStyle(bigOutputContact);
                    isRootGate = false;
                }
            }
        }
        propagateInitValues(circuit, nonRootGates);
    }

    private String gateToString(VisualCircuit circuit, VisualFunctionComponent gate) {
        String gateRef = circuit.getNodeMathReference(gate);

        VisualFunctionContact outputContact = gate.getGateOutput();
        String outputName = outputContact.getName();

        BooleanFormula setFunction = outputContact.getSetFunction();
        String functionString = FormulaToString.toString(setFunction);
        return gateRef + " [" + outputName + " = " + functionString + "]";
    }

    private int getSplitGateCount(SplitForm functions) {
        int count = 0;
        for (BooleanFormula function: functions.getClauses()) {
            if (function instanceof BooleanVariable) continue;
            count++;
        }
        return count;
    }

    private void connectTerminal(VisualCircuit circuit, Iterator<NodeConnectionPair> fromNodeConnectionIterator,
            Stack<Set<NodeConnectionPair>> toNodeConnectionsStack) {

        NodeConnectionPair fromNodeConnection = fromNodeConnectionIterator.next();
        if (!toNodeConnectionsStack.isEmpty()) {
            Set<NodeConnectionPair> toNodeConnections = toNodeConnectionsStack.pop();
            connectFanoutCopyFrom(circuit, fromNodeConnection, toNodeConnections);
        }
    }

    private VisualFunctionComponent insertGate(VisualCircuit circuit, BooleanFormula function,
            Container container, Stack<Set<NodeConnectionPair>> toNodeConnectionsStack) {

        FunctionComponent mathGate = new FunctionComponent();
        Container mathContainer = NamespaceHelper.getMathContainer(circuit, container);
        mathContainer.add(mathGate);

        VisualFunctionComponent gate = circuit.createVisualComponent(mathGate, VisualFunctionComponent.class, container);
        VisualFunctionContact outputContact = createGateOutput(circuit, gate, function);
        LogUtils.logInfo("  - " + gateToString(circuit, gate));

        if (!toNodeConnectionsStack.isEmpty()) {
            Set<NodeConnectionPair> toNodeConnections = toNodeConnectionsStack.pop();
            connectFanout(circuit, outputContact, toNodeConnections);
            if (toNodeConnections.size() == 1) {
                NodeConnectionPair nodeConnection = toNodeConnections.iterator().next();
                VisualComponent toNode = (VisualComponent) nodeConnection.getFirst();
                double x = toNode.getRootSpaceX() - 1.0;
                double y = toNode.getRootSpaceY();
                gate.setRootSpacePosition(new Point2D.Double(x, y));
            }
        }

        List<VisualContact> inputContacts = gate.getVisualInputs();
        Collections.reverse(inputContacts);
        for (VisualContact inputContact: inputContacts) {
            Set<NodeConnectionPair> toNodes = new HashSet<>();
            toNodes.add(new NodeConnectionPair(inputContact, null));
            toNodeConnectionsStack.push(toNodes);
        }
        return gate;
    }

    private List<NodeConnectionPair> getComponentDriverNodes(VisualCircuit circuit, VisualFunctionComponent component) {
        List<NodeConnectionPair> result = new LinkedList<>();
        VisualFunctionContact outputContact = component.getGateOutput();
        BooleanFormula setFunction = outputContact.getSetFunction();
        List<BooleanVariable> orderedLiterals = setFunction.accept(new FormulaToLiterals());
        for (BooleanVariable literal: orderedLiterals) {
            VisualContact inputContact = circuit.getVisualComponent((MathNode) literal, VisualContact.class);
            VisualNode driver = null;
            VisualConnection visualConnection = null;
            for (Connection connection: circuit.getConnections(inputContact)) {
                if (!(connection instanceof VisualConnection)) continue;
                visualConnection = (VisualConnection) connection;
                driver = visualConnection.getFirst();
                break;
            }
            result.add(new NodeConnectionPair(driver, visualConnection));
        }
        return result;
    }

    private Set<NodeConnectionPair> getComponentNonLoopDrivenNodes(VisualCircuit circuit, VisualFunctionComponent component) {
        Set<NodeConnectionPair> result = new HashSet<>();
        for (VisualContact outputContact: component.getVisualOutputs()) {
            for (Connection connection: circuit.getConnections(outputContact)) {
                if (!CircuitUtils.isSelfLoop(connection) && (connection instanceof VisualConnection)) {
                    VisualConnection visualConnection = (VisualConnection) connection;
                    result.add(new NodeConnectionPair(visualConnection.getSecond(), visualConnection));
                }
            }
        }
        return result;
    }

    private VisualFunctionContact createGateOutput(VisualCircuit circuit, VisualFunctionComponent component,
            BooleanFormula function) {

        String outputName = (function instanceof Not) ? "ON" : "O";
        VisualFunctionContact outputContact = circuit.getOrCreateContact(component, outputName, IOType.OUTPUT);
        try {
            String formulaString = FormulaToString.toString(function);
            BooleanFormula setFuncton = CircuitUtils.parseContactFuncton(circuit, component, formulaString);
            outputContact.setSetFunction(setFuncton);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
        }
        return outputContact;
    }

    private void connectFanoutCopyFrom(VisualCircuit circuit, NodeConnectionPair fromNodeConnection,
            Set<NodeConnectionPair> toNodeConnections) {

        for (NodeConnectionPair toNodeConnection: toNodeConnections) {
            if ((fromNodeConnection != null) && (toNodeConnection != null)) {
                try {
                    VisualNode fromNode = fromNodeConnection.getFirst();
                    VisualNode toNode = toNodeConnection.getFirst();
                    VisualConnection connection = (VisualConnection) circuit.connect(fromNode, toNode);
                    VisualConnection fromConnection = fromNodeConnection.getSecond();
                    connection.copyShape(fromConnection);
                    connection.copyStyle(fromConnection);
                } catch (InvalidConnectionException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void connectFanout(VisualCircuit circuit, VisualNode fromNode, Set<NodeConnectionPair> toNodeConnections) {
        for (NodeConnectionPair toNodeConnection: toNodeConnections) {
            if ((fromNode != null) && (toNodeConnection != null)) {
                try {
                    VisualNode toNode = toNodeConnection.getFirst();
                    VisualConnection toConnection = toNodeConnection.getSecond();
                    VisualConnection connection = (VisualConnection) circuit.connect(fromNode, toNode);
                    connection.copyShape(toConnection);
                    connection.copyStyle(toConnection);
                } catch (InvalidConnectionException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void propagateInitValues(VisualCircuit circuit, LinkedList<VisualFunctionComponent> gates) {
        for (VisualFunctionComponent gate: gates) {
            VisualFunctionContact output = gate.getGateOutput();
            if (output != null) {
                LinkedList<BooleanVariable> variables = new LinkedList<>();
                LinkedList<BooleanFormula> values = new LinkedList<>();
                for (VisualContact input: gate.getVisualInputs()) {
                    VisualContact driver = CircuitUtils.findDriver(circuit, input, false);
                    if (driver != null) {
                        BooleanVariable variable = input.getReferencedContact();
                        variables.add(variable);
                        boolean initToOne = driver.getReferencedContact().getInitToOne();
                        BooleanFormula value = initToOne ? One.instance() : Zero.instance();
                        values.add(value);
                    }
                }
                BooleanFormula setFunction = BooleanUtils.cleverReplace(output.getSetFunction(), variables, values);
                boolean isOne = One.instance().equals(setFunction);
                output.getReferencedContact().setInitToOne(isOne);
            }
        }
    }

}
