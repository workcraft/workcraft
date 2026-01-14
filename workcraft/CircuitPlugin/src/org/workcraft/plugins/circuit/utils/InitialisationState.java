package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.utils.SortUtils;

import java.util.*;
import java.util.stream.Collectors;

public class InitialisationState {

    private static final BooleanWorker WORKER = CleverBooleanWorker.getInstance();

    private final Circuit circuit;
    public final List<Contact> driverPorts = new ArrayList<>();
    private final List<Contact> driverPins = new ArrayList<>();
    private final Set<MathNode> highSet = new HashSet<>();
    private final Set<MathNode> lowSet = new HashSet<>();
    private final Set<MathNode> conflictSet = new HashSet<>();
    private final Set<MathNode> problematicSet = new HashSet<>();

    public InitialisationState(Circuit circuit) {
        this.circuit = circuit;
        driverPorts.addAll(circuit.getInputPorts());
        SortUtils.sortNatural(driverPorts, circuit::getNodeReference);

        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay()) {
                driverPins.addAll(component.getOutputs());
            }
        }
        SortUtils.sortNatural(driverPins, circuit::getNodeReference);

        Queue<MathConnection> queue = new ArrayDeque<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && isForcedOrConstant(contact)) {
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
                if (conflictSet.contains(fromNode)) {
                    conflictSet.add(connection);
                }
                MathNode toNode = connection.getSecond();
                if (nodeInitLevelSet.add(toNode)) {
                    Node parent = toNode.getParent();
                    if (parent instanceof FunctionComponent component) {
                        propagateValuesToOutputs(circuit, component, queue);
                    } else {
                        Set<MathConnection> connections = circuit.getConnections(toNode);
                        queue.addAll(connections);
                    }
                }
            }
        }
        problematicSet.addAll(ResetUtils.getProblematicPins(circuit));
    }

    private boolean isForcedOrConstant(FunctionContact contact) {
        return contact.getForcedInit() || CircuitUtils.isConstantDriver0(contact) || CircuitUtils.isConstantDriver1(contact);
    }

    private void fillVariableValues(FunctionComponent component,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {

        for (FunctionContact contact : component.getFunctionContacts()) {
            Set<MathNode> contactInitLevelSet = chooseNodeLevelSet(contact);
            if (contactInitLevelSet != null) {
                variables.add(contact);
                values.add(contactInitLevelSet == highSet ? One.getInstance() : Zero.getInstance());
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
                        conflictSet.add(outputPin);
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
        BooleanFormula setFunction = FormulaUtils.replace(contact.getSetFunction(), variables, values, WORKER);
        BooleanFormula resetFunction = FormulaUtils.replace(contact.getResetFunction(), variables, values, WORKER);
        if (ResetUtils.isEvaluatedHigh(setFunction, resetFunction)) {
            return highSet;
        }
        if (ResetUtils.isEvaluatedLow(setFunction, resetFunction)) {
            return lowSet;
        }
        return null;
    }

    public boolean isHigh(MathNode node) {
        return highSet.contains(node);
    }

    public boolean isLow(MathNode node) {
        return lowSet.contains(node);
    }

    public boolean isConflict(MathNode node) {
        return conflictSet.contains(node);
    }

    public boolean isProblematic(MathNode node) {
        return problematicSet.contains(node);
    }

    public boolean isInitialisedPin(Contact contact) {
        return !isConflict(contact) && (isLow(contact) || isHigh(contact));
    }

    public boolean isProblematicPin(Contact contact) {
        return isConflict(contact) || (!isLow(contact) && !isHigh(contact) && isProblematic(contact));
    }

    public String getContactReference(Contact contact) {
        return circuit.getNodeReference(contact);
    }

    public int getDriverPortCount() {
        return driverPorts.size();
    }

    public Contact getDriverPort(int index) {
        return (index >= 0) && (index < driverPorts.size()) ? driverPorts.get(index) : null;
    }

    public Set<Contact> getForcedPorts() {
        return driverPorts.stream()
                .filter(Contact::getForcedInit)
                .collect(Collectors.toSet());
    }

    public int getDriverPinCount() {
        return driverPins.size();
    }

    public Contact getDriverPin(int index) {
        return (index >= 0) && (index < driverPins.size()) ? driverPins.get(index) : null;
    }

    public Collection<Contact> getUninitialisedPins() {
        Set<Contact> result = new HashSet<>(driverPins);
        result.removeAll(conflictSet);
        result.removeAll(highSet);
        result.removeAll(lowSet);
        return result;
    }

    public Set<Contact> getForcedPins() {
        return driverPins.stream()
                .filter(Contact::getForcedInit)
                .collect(Collectors.toSet());
    }

    public Set<Contact> getProblematicPins() {
        Set<Contact> result = new HashSet<>(driverPins);
        result.retainAll(problematicSet);
        return result;
    }

    public boolean isRedundantForcedInit(FunctionContact contact) {
        return false;
    }

}
