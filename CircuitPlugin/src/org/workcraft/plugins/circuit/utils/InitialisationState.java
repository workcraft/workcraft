package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;

import java.util.*;

public class InitialisationState {

    private final HashSet<Node> highSet = new HashSet<>();
    private final HashSet<Node> lowSet = new HashSet<>();
    private final HashSet<Node> errorSet = new HashSet<>();
    private final ArrayList<String> forcedPins = new ArrayList<>();

    public InitialisationState(Circuit circuit) {
        Queue<Connection> queue = new LinkedList<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit()) {
                String pinRef = circuit.getNodeReference(contact);
                forcedPins.add(pinRef);
                HashSet<Node> initSet = contact.getInitToOne() ? highSet : lowSet;
                if (initSet.add(contact)) {
                    queue.addAll(circuit.getConnections(contact));
                }
            }
        }
        Collections.sort(forcedPins);

        while (!queue.isEmpty()) {
            Connection connection = queue.remove();
            Node fromNode = connection.getFirst();
            HashSet<Node> nodeInitLevelSet = chooseNodeLevelSet(fromNode);
            if ((nodeInitLevelSet != null) && nodeInitLevelSet.add(connection)) {
                if (errorSet.contains(fromNode)) {
                    errorSet.add(connection);
                }
                Node toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent) {
                        FunctionComponent component = (FunctionComponent) parent;
                        propagateValuesToOutputs(circuit, component, queue);
                    } else {
                        Set<Connection> connections = circuit.getConnections(toNode);
                        queue.addAll(connections);
                    }
                }
            }
        }

    }
    private void fillVariableValues(FunctionComponent component,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {
        for (FunctionContact contact : component.getFunctionContacts()) {
            HashSet<Node> contactInitLevelSet = chooseNodeLevelSet(contact);
            if (contactInitLevelSet != null) {
                variables.add(contact);
                values.add(contactInitLevelSet == highSet ? One.instance() : Zero.instance());
            }
        }
    }

    private void propagateValuesToOutputs(Circuit circuit, FunctionComponent component, Queue<Connection> queue) {
        boolean progress = true;
        while (progress) {
            progress = false;
            LinkedList<BooleanVariable> variables = new LinkedList<>();
            LinkedList<BooleanFormula> values = new LinkedList<>();
            fillVariableValues(component, variables, values);
            for (FunctionContact outputPin : component.getFunctionOutputs()) {
                Set<Node> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values);
                if ((outputInitLevelSet != null) && outputInitLevelSet.add(outputPin)) {
                    progress = true;
                    if (!outputPin.getForcedInit() && ((outputInitLevelSet == highSet) != outputPin.getInitToOne())) {
                        errorSet.add(outputPin);
                    }
                    queue.addAll(circuit.getConnections(outputPin));
                }
            }
        }
    }

    private HashSet<Node> chooseNodeLevelSet(Node node) {
        if (highSet.contains(node)) {
            return highSet;
        }
        if (lowSet.contains(node)) {
            return lowSet;
        }
        return null;
    }

    private HashSet<Node> chooseFunctionLevelSet(FunctionContact contact,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {
        if (contact.getForcedInit()) {
            return contact.getInitToOne() ? highSet : lowSet;
        }
        BooleanFormula setFunction = BooleanUtils.replaceClever(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.replaceClever(contact.getResetFunction(), variables, values);
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

    public boolean isCorrectlyInitialised(Node contact) {
        return !errorSet.contains(contact) && (lowSet.contains(contact) || highSet.contains(contact));
    }

    public boolean isHigh(Node contact) {
        return highSet.contains(contact);
    }

    public boolean isLow(Node contact) {
        return lowSet.contains(contact);
    }

    public boolean isError(Node node) {
        return errorSet.contains(node);
    }

    public String getForcedPin(int index) {
        return (index < forcedPins.size()) ? forcedPins.get(index) : null;
    }

    public int getForcedPinCount() {
        return forcedPins.size();
    }

}
