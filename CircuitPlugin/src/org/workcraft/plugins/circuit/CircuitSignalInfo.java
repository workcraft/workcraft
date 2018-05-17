package org.workcraft.plugins.circuit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.utils.BooleanUtils;

public class CircuitSignalInfo {

    public static class SignalInfo {
        public final FunctionContact contact;
        public final BooleanFormula setFormula;
        public final BooleanFormula resetFormula;

        public SignalInfo(FunctionContact contact, BooleanFormula setFormula, BooleanFormula resetFormula) {
            this.contact = contact;
            this.setFormula = setFormula;
            this.resetFormula = resetFormula;
        }
    }

    public final Circuit circuit;
    private final HashMap<Contact, String> contactSignalMap = new HashMap<>();
    private final HashMap<String, BooleanFormula> signalLiteralMap;

    public CircuitSignalInfo(Circuit circuit) {
        this(circuit, null, null);
    }

    public CircuitSignalInfo(Circuit circuit, String literalPrefix, String literalSuffix) {
        this.circuit = circuit;
        this.signalLiteralMap = buildSignalLiteralMap(literalPrefix, literalSuffix);
    }

    public Circuit getCircuit() {
        return circuit;
    }

    private HashMap<String, BooleanFormula> buildSignalLiteralMap(String literalPrefix, String literalSuffix) {
        HashMap<String, BooleanFormula> result = new HashMap<>();
        if (literalPrefix == null) {
            literalPrefix = "";
        }
        if (literalSuffix == null) {
            literalSuffix = "";
        }
        for (FunctionContact contact: circuit.getFunctionContacts()) {
            if (contact.isDriver()) {
                String signalName = getContactSignal(contact);
                String literalName = literalPrefix + signalName + literalSuffix;
                BooleanFormula literal = new Literal(literalName);
                result.put(signalName, literal);
            }
        }
        return result;
    }

    public String getContactSignal(Contact contact) {
        String result = contactSignalMap.get(contact);
        if (result == null) {
            if (!circuit.getPreset(contact).isEmpty() || !circuit.getPostset(contact).isEmpty()) {
                Contact signal = CircuitUtils.findSignal(circuit, contact, false);
                Node parent = signal.getParent();
                boolean isAssignOutput = false;
                if (parent instanceof FunctionComponent) {
                    FunctionComponent component = (FunctionComponent) parent;
                    isAssignOutput = signal.isOutput() && !component.isMapped();
                }
                if (isAssignOutput) {
                    result = CircuitUtils.getSignalName(circuit, signal);
                } else {
                    result = CircuitUtils.getContactName(circuit, signal);
                }
            }
            if (result != null) {
                if (NamespaceHelper.isHierarchical(result)) {
                    HierarchicalUniqueNameReferenceManager refManager
                            = (HierarchicalUniqueNameReferenceManager) circuit.getReferenceManager();

                    NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(circuit.getRoot());
                    NameManager nameManagerer = refManager.getNameManager(namespaceProvider);
                    String candidateName = NamespaceHelper.flattenReference(result);
                    result = nameManagerer.getDerivedName(contact, candidateName);
                }
                contactSignalMap.put(contact, result);
            }
        }
        return result;
    }

    public Collection<SignalInfo> getComponentSignalInfos(FunctionComponent component) {
        Collection<SignalInfo> result = new ArrayList<>();
        LinkedList<BooleanVariable> variables = new LinkedList<>();
        LinkedList<BooleanFormula> values = new LinkedList<>();
        for (FunctionContact contact: component.getFunctionContacts()) {
            String signalName = getContactSignal(contact);
            BooleanFormula literal = signalLiteralMap.get(signalName);
            if (literal != null) {
                variables.add(contact);
                values.add(literal);
            }
        }
        for (FunctionContact contact: component.getFunctionContacts()) {
            if (contact.isOutput()) {
                BooleanFormula setFunction = BooleanUtils.replaceClever(contact.getSetFunction(), variables, values);
                BooleanFormula resetFunction = BooleanUtils.replaceClever(contact.getResetFunction(), variables, values);
                SignalInfo signalInfo = new SignalInfo(contact, setFunction, resetFunction);
                result.add(signalInfo);
            }
        }
        return result;
    }

    public String getComponentReference(FunctionComponent component) {
        return circuit.getNodeReference(component);
    }

    public String getComponentFlattenReference(FunctionComponent component) {
        String ref = getComponentReference(component);
        return NamespaceHelper.flattenReference(ref);
    }

}
