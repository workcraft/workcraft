package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropagateInversionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private static final Pattern PIN_NAME_PATTERN = Pattern.compile("(([ABCDEF][0-9]?)|O)N?");

    @Override
    public String getDisplayName() {
        return "Propagate inversion through gates (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Propagate inversion through gate";
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
    public Collection<VisualNode> collectNodes(VisualModel model) {
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
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualFunctionComponent)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualFunctionComponent component = (VisualFunctionComponent) node;
            if (component.isGate()) {
                transformGate(circuit, component);
            }
        }
    }

    private void transformGate(VisualCircuit circuit, VisualFunctionComponent gate) {
        VisualFunctionContact outputContact = gate.getGateOutput();
        String gateStr = CircuitUtils.cellToString(circuit, gate);

        BooleanFormula setFunction = outputContact.getSetFunction();
        if (setFunction == null) {
            LogUtils.logWarning("Gate " + gateStr + " cannot be transformed as it does not have set function defined");
            return;
        }
        BooleanFormula newSetFunction = FormulaUtils.propagateInversion(setFunction);

        BooleanFormula resetFunction = outputContact.getResetFunction();
        BooleanFormula newResetFunction = FormulaUtils.propagateInversion(resetFunction);

        if (!compareFunctions(setFunction, newSetFunction) || !compareFunctions(resetFunction, newResetFunction)) {
            outputContact.setSetFunction(newSetFunction);
            gate.clearMapping();
            renameContacts(circuit.getMathModel(), gate.getReferencedComponent());
            String newGateStr = CircuitUtils.cellToString(circuit, gate);
            LogUtils.logInfo("Transforming gate " + gateStr + " into " + newGateStr);
            circuit.addToSelection(gate);
        }
    }

    private boolean compareFunctions(BooleanFormula func1, BooleanFormula func2) {
        String str1 = StringGenerator.toString(func1);
        String str2 = StringGenerator.toString(func2);
        return str1.equals(str2);
    }

    private void renameContacts(Circuit circuit, FunctionComponent gate) {
        if (checkContactNamePattern(gate)) {
            Set<FunctionContact> bubbleContacts = CircuitUtils.getBubbleContacts(gate);
            for (Contact contact: gate.getContacts()) {
                renameContactBubbleHeuristic(circuit, contact, bubbleContacts);
            }
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A", "BN"),
                    Arrays.asList("B", "AN"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A", "B", "CN"),
                    Arrays.asList("C", "B", "AN"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A", "BN", "CN"),
                    Arrays.asList("C", "BN", "AN"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A", "B", "C", "DN"),
                    Arrays.asList("D", "B", "C", "AN"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A", "B", "CN", "DN"),
                    Arrays.asList("D", "C", "BN", "AN"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A", "BN", "CN", "DN"),
                    Arrays.asList("D", "BN", "CN", "AN"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A1", "A2N"),
                    Arrays.asList("A2", "A1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A1", "A2", "A3N"),
                    Arrays.asList("A3", "A2", "A1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("A1", "A2N", "A3N"),
                    Arrays.asList("A3", "A2N", "A1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("B1", "B2N"),
                    Arrays.asList("B2", "B1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("B1", "B2", "B3N"),
                    Arrays.asList("B3", "B2", "B1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("B1", "B2N", "B3N"),
                    Arrays.asList("B3", "B2N", "B1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("C1", "C2N"),
                    Arrays.asList("C2", "C1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("C1", "C2", "C3N"),
                    Arrays.asList("C3", "C2", "C1N"));
            renameContactsOrderHeuristic(circuit, gate,
                    Arrays.asList("C1", "C2N", "C3N"),
                    Arrays.asList("C3", "C2N", "C1N"));
        }
    }

    private boolean checkContactNamePattern(FunctionComponent gate) {
        if (gate.getInputs().size() < 2) {
            return false;
        }
        for (Contact contact: gate.getContacts()) {
            Matcher matcher = PIN_NAME_PATTERN.matcher(contact.getName());
            if (!matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    private void renameContactBubbleHeuristic(Circuit circuit, Contact contact, Set<FunctionContact> bubbleContacts) {
        String name = circuit.getName(contact);
        if (name.endsWith("N") && !bubbleContacts.contains(contact)) {
            name = name.substring(0, name.length() - 1);
        } else if (!name.endsWith("N") && bubbleContacts.contains(contact)) {
            name += "N";
        }
        circuit.setName(contact, name);
    }

    private void renameContactsOrderHeuristic(Circuit circuit, FunctionComponent gate, List<String> srcNames, List<String> dstNames) {
        Map<String, Contact> nameToContactMap = new HashMap<>();
        for (Contact contact: gate.getContacts()) {
            nameToContactMap.put(contact.getName(), contact);
        }
        Set<String> names = nameToContactMap.keySet();
        Set<String> srcSet = new HashSet<>(srcNames);
        if (names.containsAll(srcSet)) {
            int len = Math.min(srcNames.size(), dstNames.size());
            for (int i = 0; i < len; i++) {
                String srcName = srcNames.get(i);
                String dstName = dstNames.get(i);
                if (srcName.equals(dstName)) continue;
                Contact contact = nameToContactMap.get(srcName);
                if (contact != null) {
                    circuit.setName(contact, dstName);
                }
            }
        }
    }

}
