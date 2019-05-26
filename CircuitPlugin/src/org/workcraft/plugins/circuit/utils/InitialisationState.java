package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class InitialisationState {

    private final Set<MathNode> highSet = new HashSet<>();
    private final Set<MathNode> lowSet = new HashSet<>();
    private final Set<MathNode> errorSet = new HashSet<>();

    public InitialisationState(Circuit circuit) {
        Queue<MathConnection> queue = new LinkedList<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit()) {
                Set<MathNode> initSet = contact.getInitToOne() ? highSet : lowSet;
                if (initSet.add(contact)) {
                    queue.addAll(circuit.getConnections(contact));
                }
            }
        }

        while (!queue.isEmpty()) {
            MathConnection connection = queue.remove();
            MathNode fromNode = connection.getFirst();
            Set<MathNode> nodeInitLevelSet = chooseNodeLevelSet(fromNode);
            if ((nodeInitLevelSet != null) && nodeInitLevelSet.add(connection)) {
                if (errorSet.contains(fromNode)) {
                    errorSet.add(connection);
                }
                MathNode toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent) {
                        FunctionComponent component = (FunctionComponent) parent;
                        propagateValuesToOutputs(circuit, component, queue);
                    } else {
                        Set<MathConnection> connections = circuit.getConnections(toNode);
                        queue.addAll(connections);
                    }
                }
            }
        }
    }

    private void fillVariableValues(FunctionComponent component,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {

        for (FunctionContact contact : component.getFunctionContacts()) {
            Set<MathNode> contactInitLevelSet = chooseNodeLevelSet(contact);
            if (contactInitLevelSet != null) {
                variables.add(contact);
                values.add(contactInitLevelSet == highSet ? One.instance() : Zero.instance());
            }
        }
    }

    private void propagateValuesToOutputs(Circuit circuit, FunctionComponent component, Queue<MathConnection> queue) {
        boolean progress = true;
        while (progress) {
            progress = false;
            LinkedList<BooleanVariable> variables = new LinkedList<>();
            LinkedList<BooleanFormula> values = new LinkedList<>();
            fillVariableValues(component, variables, values);
            for (FunctionContact outputPin : component.getFunctionOutputs()) {
                Set<MathNode> outputInitLevelSet = chooseFunctionLevelSet(outputPin, variables, values);
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

    private Set<MathNode> chooseNodeLevelSet(MathNode node) {
        if (highSet.contains(node)) {
            return highSet;
        }
        if (lowSet.contains(node)) {
            return lowSet;
        }
        return null;
    }

    private Set<MathNode> chooseFunctionLevelSet(FunctionContact contact,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {

        if (contact.getForcedInit()) {
            return contact.getInitToOne() ? highSet : lowSet;
        }
        BooleanFormula setFunction = BooleanUtils.replaceClever(contact.getSetFunction(), variables, values);
        BooleanFormula resetFunction = BooleanUtils.replaceClever(contact.getResetFunction(), variables, values);
        if (ResetUtils.isEvaluatedHigh(setFunction, resetFunction)) {
            return highSet;
        }
        if (ResetUtils.isEvaluatedLow(setFunction, resetFunction)) {
            return lowSet;
        }
        return null;
    }

    public boolean isCorrectlyInitialised(MathNode contact) {
        return !errorSet.contains(contact) && (lowSet.contains(contact) || highSet.contains(contact));
    }

    public boolean isHigh(MathNode contact) {
        return highSet.contains(contact);
    }

    public boolean isLow(MathNode contact) {
        return lowSet.contains(contact);
    }

    public boolean isError(MathNode node) {
        return errorSet.contains(node);
    }

}
