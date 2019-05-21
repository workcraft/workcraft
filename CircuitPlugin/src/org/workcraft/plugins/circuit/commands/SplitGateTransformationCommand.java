package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Not;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.naryformula.SplitForm;
import org.workcraft.plugins.circuit.naryformula.SplitFormGenerator;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.GateUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.types.Pair;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.awt.geom.Point2D;
import java.util.*;

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
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualFunctionComponent) && ((VisualFunctionComponent) node).isGate();
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Collection<VisualNode> collect(VisualModel model) {
        Collection<VisualNode> components = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            components.addAll(Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualFunctionComponent.class));
            Collection<VisualNode> selection = circuit.getSelection();
            if (!selection.isEmpty()) {
                components.retainAll(selection);
            }
        }
        return components;
    }

    @Override
    public void transform(VisualModel model, VisualNode node) {
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
        String str = CircuitUtils.gateToString(circuit, bigGate);
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

        LogUtils.logInfo("Splitting multi-level gate " + str + " into:");
        List<NodeConnectionPair> fromNodeConnections = getComponentDriverNodes(circuit, bigGate);
        Set<NodeConnectionPair> toNodeConnections = getComponentNonLoopDrivenNodes(circuit, bigGate);
        Container container = (Container) bigGate.getParent();
        circuit.remove(bigGate);

        Stack<Set<NodeConnectionPair>> toNodeConnectionsStack = new Stack<>();
        toNodeConnectionsStack.push(toNodeConnections);

        Iterator<NodeConnectionPair> fromNodeConnectionIterator = fromNodeConnections.iterator();
        boolean isRootGate = true;
        Direction direction = bigOutputContact.getDirection();
        LinkedList<VisualFunctionComponent> nonRootGates = new LinkedList<>();
        for (BooleanFormula function : functions.getClauses()) {
            if (function instanceof BooleanVariable) {
                connectTerminal(circuit, fromNodeConnectionIterator, toNodeConnectionsStack);
            } else {
                VisualFunctionComponent gate = insertGate(circuit, function, container, toNodeConnectionsStack, direction);
                gate.copyStyle(bigGate);
                gate.getReferencedComponent().setModule("");
                if (!isRootGate) {
                    nonRootGates.push(gate);
                } else {
                    Point2D offset = getOffset(direction);
                    Point2D pos = new Point2D.Double(bigGate.getX() - offset.getX(), bigGate.getY() - offset.getY());
                    gate.setPosition(pos);
                    VisualFunctionContact outputContact = gate.getGateOutput();
                    // Update fromNodes for self-loops
                    fromNodeConnections.replaceAll(pair -> (pair.getFirst() == bigOutputContact)
                            ? new NodeConnectionPair(outputContact, pair.getSecond()) : pair);
                    outputContact.copyStyle(bigOutputContact);
                    isRootGate = false;
                }
                circuit.addToSelection(gate);
            }
        }
        propagateInitValues(circuit, nonRootGates);
    }

    private int getSplitGateCount(SplitForm functions) {
        int count = 0;
        for (BooleanFormula function : functions.getClauses()) {
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

    private VisualFunctionComponent insertGate(VisualCircuit circuit, BooleanFormula function, Container container,
            Stack<Set<NodeConnectionPair>> toNodeConnectionsStack, Direction direction) {

        FunctionComponent mathGate = new FunctionComponent();
        Container mathContainer = NamespaceHelper.getMathContainer(circuit, container);
        mathContainer.add(mathGate);

        VisualFunctionComponent gate = circuit.createVisualComponent(mathGate, VisualFunctionComponent.class, container);
        VisualFunctionContact outputContact = createGateOutput(circuit, gate, function);
        LogUtils.logInfo("  - " + CircuitUtils.gateToString(circuit, gate));

        outputContact.setDirection(direction);
        Point2D offset = getOffset(direction);
        if (!toNodeConnectionsStack.isEmpty()) {
            Set<NodeConnectionPair> toNodeConnections = toNodeConnectionsStack.pop();
            connectFanout(circuit, outputContact, toNodeConnections);
            if (toNodeConnections.size() == 1) {
                NodeConnectionPair nodeConnection = toNodeConnections.iterator().next();
                VisualComponent toNode = (VisualComponent) nodeConnection.getFirst();
                double x = toNode.getRootSpaceX() + offset.getX();
                double y = toNode.getRootSpaceY() + offset.getY();
                gate.setRootSpacePosition(new Point2D.Double(x, y));
            }
        }

        List<VisualContact> inputContacts = gate.getVisualInputs();
        Collections.reverse(inputContacts);
        for (VisualContact inputContact : inputContacts) {
            Set<NodeConnectionPair> toNodes = new HashSet<>();
            toNodes.add(new NodeConnectionPair(inputContact, null));
            toNodeConnectionsStack.push(toNodes);
        }
        return gate;
    }

    private Point2D getOffset(Direction direction) {
        switch (direction) {
        case WEST: return new Point2D.Double(1.0, 0.0);
        case NORTH: return new Point2D.Double(0.0, 1.0);
        case EAST: return new Point2D.Double(-1.0, 0.0);
        case SOUTH: return new Point2D.Double(0.0, -1.0);
        default: return new Point2D.Double(0.0, 0.0);
        }
    }

    private List<NodeConnectionPair> getComponentDriverNodes(VisualCircuit circuit, VisualFunctionComponent component) {
        List<NodeConnectionPair> result = new LinkedList<>();
        for (VisualContact inputContact : component.getOrderedVisualFunctionContacts()) {
            VisualNode driver = null;
            VisualConnection visualConnection = null;
            for (VisualConnection connection : circuit.getConnections(inputContact)) {
                driver = connection.getFirst();
                break;
            }
            result.add(new NodeConnectionPair(driver, visualConnection));
        }
        return result;
    }

    private Set<NodeConnectionPair> getComponentNonLoopDrivenNodes(VisualCircuit circuit, VisualFunctionComponent component) {
        Set<NodeConnectionPair> result = new HashSet<>();
        for (VisualContact outputContact : component.getVisualOutputs()) {
            for (VisualConnection connection : circuit.getConnections(outputContact)) {
                if (!CircuitUtils.isSelfLoop(connection)) {
                    result.add(new NodeConnectionPair(connection.getSecond(), connection));
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
            String formulaString = StringGenerator.toString(function);
            BooleanFormula setFuncton = CircuitUtils.parsePinFuncton(circuit, component, formulaString);
            outputContact.setSetFunction(setFuncton);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return outputContact;
    }

    private void connectFanoutCopyFrom(VisualCircuit circuit, NodeConnectionPair fromNodeConnection,
            Set<NodeConnectionPair> toNodeConnections) {

        for (NodeConnectionPair toNodeConnection : toNodeConnections) {
            if ((fromNodeConnection != null) && (toNodeConnection != null)) {
                try {
                    VisualNode fromNode = fromNodeConnection.getFirst();
                    VisualNode toNode = toNodeConnection.getFirst();
                    VisualConnection connection = circuit.connect(fromNode, toNode);
                    VisualConnection fromConnection = fromNodeConnection.getSecond();
                    connection.copyShape(fromConnection);
                    connection.copyStyle(fromConnection);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
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
                    VisualConnection connection = circuit.connect(fromNode, toNode);
                    connection.copyShape(toConnection);
                    connection.copyStyle(toConnection);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void propagateInitValues(VisualCircuit circuit, LinkedList<VisualFunctionComponent> gates) {
        for (VisualFunctionComponent gate: gates) {
            GateUtils.propagateInitialState(circuit, gate);
        }
    }

}
