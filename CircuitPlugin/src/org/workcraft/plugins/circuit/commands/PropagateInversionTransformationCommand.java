package org.workcraft.plugins.circuit.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropagateInversionTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private static final Pattern PIN_NAME_PATTERN = Pattern.compile("(([ABCDEF][0-9]?)|O)N?");

    @Override
    public String getDisplayName() {
        return "Propagate inversion through gates (selected or all)";
        //return "Apply De Morgan law to gates (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Propagate inversion through gate";
        //return "Apply De Morgan law to gate";
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

    private void transformGate(VisualCircuit circuit, VisualFunctionComponent gate) {
        VisualFunctionContact outputContact = gate.getGateOutput();
        String gateStr = CircuitUtils.gateToString(circuit, gate);

        BooleanFormula setFunction = outputContact.getSetFunction();
        BooleanFormula resetFunction = outputContact.getResetFunction();
        if (setFunction == null) {
            LogUtils.logWarning("Gate " + gateStr + " cannot be transformed as it does not have set functions defined");
            return;
        }

        BooleanFormula newSetFunction = BooleanUtils.propagateInversion(setFunction);
        BooleanFormula newResetFunction = BooleanUtils.propagateInversion(resetFunction);
        if (!BooleanUtils.compareFunctions(setFunction, newSetFunction)
                || !BooleanUtils.compareFunctions(resetFunction, newResetFunction)) {
            outputContact.setSetFunction(newSetFunction);
            gate.setLabel("");
            renameContacts(circuit.getMathModel(), gate.getReferencedFunctionComponent());
            String newGateStr = CircuitUtils.gateToString(circuit, gate);
            LogUtils.logInfo("Transforming gate " + gateStr + " into " + newGateStr);
            circuit.addToSelection(gate);
        }
    }

    private void renameContacts(Circuit circuit, FunctionComponent gate) {
        if (checkContactNamePattern(gate)) {
            Set<FunctionContact> bubbleContacts = CircuitUtils.getBubbleContacts(gate);
            for (Contact contact: gate.getContacts()) {
                renameContactBubbleHeuristic(circuit, contact, bubbleContacts);
            }
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A", "BN"},
                    new String[] {"B", "AN"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A", "B", "CN"},
                    new String[] {"C", "B", "AN"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A", "BN", "CN"},
                    new String[] {"C", "BN", "AN"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A", "B", "C", "DN"},
                    new String[] {"D", "B", "C", "AN"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A", "B", "CN", "DN"},
                    new String[] {"D", "C", "BN", "AN"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A", "BN", "CN", "DN"},
                    new String[] {"D", "BN", "CN", "AN"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A1", "A2N"},
                    new String[] {"A2", "A1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A1", "A2", "A3N"},
                    new String[] {"A3", "A2", "A1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"A1", "A2N", "A3N"},
                    new String[] {"A3", "A2N", "A1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"B1", "B2N"},
                    new String[] {"B2", "B1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"B1", "B2", "B3N"},
                    new String[] {"B3", "B2", "B1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"B1", "B2N", "B3N"},
                    new String[] {"B3", "B2N", "B1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"C1", "C2N"},
                    new String[] {"C2", "C1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"C1", "C2", "C3N"},
                    new String[] {"C3", "C2", "C1N"});
            renameContactsOrderHeuristic(circuit, gate,
                    new String[] {"C1", "C2N", "C3N"},
                    new String[] {"C3", "C2N", "C1N"});
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

    private void renameContactsOrderHeuristic(Circuit circuit, FunctionComponent gate, String[] srcNames, String[] dstNames) {
        Map<String, Contact> nameToContactMap = new HashMap<>();
        for (Contact contact: gate.getContacts()) {
            nameToContactMap.put(contact.getName(), contact);
        }
        Set<String> names = nameToContactMap.keySet();
        Set<String> srcSet = new HashSet<>(Arrays.asList(srcNames));
        if (names.containsAll(srcSet)) {
            int len = Math.min(srcNames.length, dstNames.length);
            for (int i = 0; i < len; i++) {
                String srcName = srcNames[i];
                String dstName = dstNames[i];
                if (srcName.equals(dstName)) continue;
                Contact contact = nameToContactMap.get(srcName);
                if (contact != null) {
                    circuit.setName(contact, dstName);
                }
            }
        }
    }

}
