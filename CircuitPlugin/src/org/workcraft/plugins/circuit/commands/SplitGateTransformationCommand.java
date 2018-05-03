package org.workcraft.plugins.circuit.commands;

import java.awt.geom.Point2D;
import java.util.Arrays;
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
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Not;
import org.workcraft.formula.jj.ParseException;
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
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class SplitGateTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Split complex gates (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Split complex gate";
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

    private void transformGate(VisualCircuit circuit, VisualFunctionComponent complexGate) {
        VisualFunctionContact outputContact = complexGate.getGateOutput();

        BooleanFormula setFunction = outputContact.getSetFunction();
        String gateString = gateToString(circuit, complexGate);
        if (setFunction == null) {
            LogUtils.logWarning("Gate " + gateString + " cannot be split as it does not have set functions defined");
            return;
        }

        BooleanFormula resetFunction = outputContact.getResetFunction();
        if ((setFunction != null) && (resetFunction != null)) {
            LogUtils.logWarning("Gate " + gateString + " cannot be split as it has both set and reset functions defined");
            return;
        }

        SplitForm functions = SplitFormGenerator.generate(setFunction);
        if (getSplitGateCount(functions) < 2) {
            LogUtils.logWarning("Gate " + gateString + " cannot be split as it is too simple");
            return;
        }

        LogUtils.logInfo("Splitting complex gate " + gateString + "into:");
        List<Node> fromNodes = getComponentDriverNodes(circuit, complexGate);
        List<Node> toNodes = getComponentDrivenNodes(circuit, complexGate);
        Container container = (Container) complexGate.getParent();
        Point2D defaultPosition = new Point2D.Double(complexGate.getX() + 1.0, complexGate.getY());
        circuit.remove(complexGate);

        Stack<List<Node>> toNodesStack = new Stack<>();
        toNodesStack.push(toNodes);

        Iterator<Node> fromNodeIterator = fromNodes.iterator();
        for (BooleanFormula function: functions.getClauses()) {
            if (function instanceof BooleanVariable) {
                connectTerminal(circuit, toNodesStack, fromNodeIterator);
            } else {
                VisualFunctionComponent gate = insertGate(circuit, function, container, toNodesStack);
                if (defaultPosition == null) continue;
                gate.setPosition(defaultPosition);
                defaultPosition = null;
            }
        }
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

    private void connectTerminal(VisualCircuit circuit, Stack<List<Node>> toNodesStack, Iterator<Node> fromNodeIterator) {
        Node fromNode = fromNodeIterator.next();
        if (!toNodesStack.isEmpty()) {
            List<Node> toNodes = toNodesStack.pop();
            connectFanout(circuit, fromNode, toNodes);
        }
    }

    private VisualFunctionComponent insertGate(VisualCircuit circuit, BooleanFormula function,
            Container container, Stack<List<Node>> toNodesStack) {

        FunctionComponent mathGate = new FunctionComponent();
        Container mathContainer = NamespaceHelper.getMathContainer(circuit, container);
        mathContainer.add(mathGate);

        VisualFunctionComponent gate = circuit.createVisualComponent(mathGate, VisualFunctionComponent.class, container);
        VisualFunctionContact outputContact = createGateOutput(circuit, gate, function);
        LogUtils.logInfo("  - " + gateToString(circuit, gate));

        if (!toNodesStack.isEmpty()) {
            List<Node> toNodes = toNodesStack.pop();
            connectFanout(circuit, outputContact, toNodes);
            alignGateWithFanout(gate, toNodes);
        }

        List<VisualContact> inputContacts = gate.getVisualInputs();
        Collections.reverse(inputContacts);
        for (VisualContact inputContact: inputContacts) {
            toNodesStack.push(Arrays.asList(inputContact));
        }
        return gate;
    }

    private List<Node> getComponentDriverNodes(VisualCircuit circuit, VisualFunctionComponent component) {
        List<Node> result = new LinkedList<>();
        for (VisualContact inputContact: component.getVisualInputs()) {
            Set<Connection> connections = circuit.getConnections(inputContact);
            if (connections.isEmpty()) {
                result.add(null);
            } else {
                for (Connection connection: connections) {
                    result.add(connection.getFirst());
                }
            }
        }
        return result;
    }

    private List<Node> getComponentDrivenNodes(VisualCircuit circuit, VisualFunctionComponent component) {
        List<Node> result = new LinkedList<>();
        VisualFunctionContact output = component.getGateOutput();
        for (Connection connection: circuit.getConnections(output)) {
            result.add(connection.getSecond());
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

    private void connectFanout(VisualCircuit circuit, Node fromNode, List<Node> toNodes) {
        for (Node toNode: toNodes) {
            if ((fromNode != null) && (toNode != null)) {
                try {
                    circuit.connect(fromNode, toNode);
                } catch (InvalidConnectionException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void alignGateWithFanout(VisualFunctionComponent component, List<Node> toNodes) {
        double xMin = 0.0;
        double ySum = 0.0;
        int count = 0;
        for (Node toNode: toNodes) {
            if (toNode instanceof VisualComponent) {
                VisualComponent toContactOrJoint = (VisualComponent) toNode;
                double x = toContactOrJoint.getRootSpaceX();
                if ((count == 0) || (x < xMin)) {
                    xMin = x;
                }
                ySum += toContactOrJoint.getRootSpaceY();
                count++;
            }
        }
        if (count > 0) {
            component.setRootSpacePosition(new Point2D.Double(xMin - 1.0, ySum / count));
        }
    }

}
