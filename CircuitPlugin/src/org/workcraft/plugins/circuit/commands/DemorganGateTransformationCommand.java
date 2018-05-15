package org.workcraft.plugins.circuit.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DemorganGateTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    private static final Pattern PIN_NAME_PATTERN = Pattern.compile("(([ABCDEF][0-9]?)|O)N?");

    @Override
    public String getDisplayName() {
        return "Apply De Morgan law to gates (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Apply De Morgan law to gate";
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

    private void transformGate(VisualCircuit circuit, VisualFunctionComponent gate) {
        VisualFunctionContact outputContact = gate.getGateOutput();

        BooleanFormula setFunction = outputContact.getSetFunction();
        String gateStr = CircuitUtils.gateToString(circuit, gate);
        if (setFunction == null) {
            LogUtils.logWarning("Gate " + gateStr + " cannot be transformed as it does not have set functions defined");
            return;
        }

        BooleanFormula resetFunction = outputContact.getResetFunction();
        if ((setFunction != null) && (resetFunction != null)) {
            LogUtils.logWarning("Gate " + gateStr + " cannot be transformed as it has both set and reset functions defined");
            return;
        }

        BooleanFormula demorganFunction = BooleanUtils.demorganProcess(setFunction);
        outputContact.setSetFunction(demorganFunction);
        gate.setLabel("");
        renameContacts(circuit.getMathModel(), gate.getReferencedFunctionComponent());
        String newGateStr = CircuitUtils.gateToString(circuit, gate);
        LogUtils.logInfo("Transforming gate " + gateStr + " into " + newGateStr);
    }

    private void renameContacts(Circuit circuit, FunctionComponent gate) {
        if (checkContactNamePattern(gate)) {
            Contact outputContact = gate.getGateOutput();
            invertContactName(circuit, outputContact);
            for (Contact contact: gate.getInputs()) {
                invertContactName(circuit, contact);
            }
            renameContactHeuristic(circuit, gate, new String[] {"A", "BN"}, new String[] {"B", "AN"});
            renameContactHeuristic(circuit, gate, new String[] {"A", "B", "CN"}, new String[] {"C", "B", "AN"});
            renameContactHeuristic(circuit, gate, new String[] {"A", "BN", "CN"}, new String[] {"C", "BN", "AN"});
            renameContactHeuristic(circuit, gate, new String[] {"A", "B", "C", "DN"}, new String[] {"D", "B", "C", "AN"});
            renameContactHeuristic(circuit, gate, new String[] {"A", "B", "CN", "DN"}, new String[] {"D", "C", "BN", "AN"});
            renameContactHeuristic(circuit, gate, new String[] {"A", "BN", "CN", "DN"}, new String[] {"D", "BN", "CN", "AN"});
            renameContactHeuristic(circuit, gate, new String[] {"A1", "A2N"}, new String[] {"A2", "A1N"});
            renameContactHeuristic(circuit, gate, new String[] {"A1", "A2", "A3N"}, new String[] {"A3", "A2", "A1N"});
            renameContactHeuristic(circuit, gate, new String[] {"A1", "A2N", "A3N"}, new String[] {"A3", "A2N", "A1N"});
            renameContactHeuristic(circuit, gate, new String[] {"B1", "B2N"}, new String[] {"B2", "B1N"});
            renameContactHeuristic(circuit, gate, new String[] {"B1", "B2", "B3N"}, new String[] {"B3", "B2", "B1N"});
            renameContactHeuristic(circuit, gate, new String[] {"B1", "B2N", "B3N"}, new String[] {"B3", "B2N", "B1N"});
            renameContactHeuristic(circuit, gate, new String[] {"C1", "C2N"}, new String[] {"C2", "C1N"});
            renameContactHeuristic(circuit, gate, new String[] {"C1", "C2", "C3N"}, new String[] {"C3", "C2", "C1N"});
            renameContactHeuristic(circuit, gate, new String[] {"C1", "C2N", "C3N"}, new String[] {"C3", "C2N", "C1N"});
        }
    }

    private void renameContactHeuristic(Circuit circuit, FunctionComponent gate, String[] srcNames, String[] dstNames) {
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

    private void invertContactName(Circuit circuit, Contact contact) {
        String name = circuit.getName(contact);
        if (name.endsWith("N")) {
            name = name.substring(0, name.length() - 1);
        } else {
            name += "N";
        }
        circuit.setName(contact, name);
    }

}
