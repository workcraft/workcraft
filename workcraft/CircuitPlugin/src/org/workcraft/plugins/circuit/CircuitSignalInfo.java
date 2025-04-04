package org.workcraft.plugins.circuit;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.NameManager;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Literal;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.TextUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CircuitSignalInfo {

    private final Circuit circuit;
    private final Map<Contact, Contact> contactDriverMap = new HashMap<>();
    private final Map<Contact, String> driverFlatNameMap = new HashMap<>();
    private final Map<Contact, String> contactAuxNameMap = new HashMap<>();
    private final Set<String> takenFlatNames = new HashSet<>();
    private final Map<String, Set<Integer>> busIndexesMap;

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

    public CircuitSignalInfo(Circuit circuit) {
        this.circuit = circuit;
        this.busIndexesMap = buildBusIndexesMap();
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public final String getContactSignal(Contact contact) {
        Contact driver = contactDriverMap.computeIfAbsent(contact,
                key -> CircuitUtils.findSignal(circuit, contact, false));

        if (driver == null) {
            return null;
        }
        String signal = driverFlatNameMap.get(driver);
        if (signal != null) {
            return signal;
        }

        Node parent = driver.getParent();
        boolean isAssignOutput = false;
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;
            isAssignOutput = driver.isOutput() && !component.isMapped();
        }
        if (isAssignOutput) {
            signal = CircuitUtils.getSignalReference(circuit, driver);
        } else {
            signal = CircuitUtils.getContactReference(circuit, driver);
        }
        if (signal != null) {
            signal = getUntakenSignal(driver, signal);
            driverFlatNameMap.put(driver, signal);
        }
        return signal;
    }

    public final String getContactAuxiliarySignal(Contact contact) {
        if (contact == null) {
            return null;
        }
        String signal = contactAuxNameMap.get(contact);
        if (signal != null) {
            return signal;
        }
        signal = CircuitUtils.getContactReference(circuit, contact);
        if (signal != null) {
            signal = getUntakenSignal(contact, signal);
            contactAuxNameMap.put(contact, signal);
        }
        return signal;
    }

    private String getUntakenSignal(Contact contact, String signal) {
        if (NamespaceHelper.isHierarchical(signal)) {
            HierarchyReferenceManager refManager = circuit.getReferenceManager();
            NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(circuit.getRoot());
            NameManager nameManager = refManager.getNameManager(namespaceProvider);
            String flatCandidateName = NamespaceHelper.flattenReference(signal);
            signal = nameManager.getDerivedName(contact, flatCandidateName);
        }
        int code = 0;
        while (takenFlatNames.contains(signal)) {
            signal = Identifier.compose(signal, TextUtils.codeToString(code));
            code++;
        }
        takenFlatNames.add(signal);
        return signal;
    }

    private Map<String, Set<Integer>> buildBusIndexesMap() {
        Set<String> signals = getCircuit().getFunctionContacts().stream()
                .filter(contact -> (contact.isPort() || contact.isDriver()))
                .map(this::getContactSignal)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Set<Integer>> result = new HashMap<>();
        Pattern pattern = CircuitSettings.getBusSignalPattern();
        for (String signal : signals) {
            Matcher matcher = pattern.matcher(signal);
            if (matcher.matches()) {
                String busName = matcher.group(1);
                Integer netIndex = Integer.valueOf(matcher.group(2));
                Set<Integer> indexes = result.computeIfAbsent(busName, key -> new HashSet<>());
                indexes.add(netIndex);
            }
        }
        return result;
    }

    public Collection<SignalInfo> getComponentSignalInfos(FunctionComponent component,
            Function<String, String> signalToLiteralName) {

        Collection<SignalInfo> result = new ArrayList<>();
        LinkedList<BooleanVariable> variables = new LinkedList<>();
        LinkedList<BooleanFormula> values = new LinkedList<>();
        Map<String, Literal> signalToLiteralMap = new HashMap<>();
        for (FunctionContact contact : component.getFunctionContacts()) {
            String signalName = getContactSignal(contact);
            String literalName = signalToLiteralName.apply(signalName);
            if (literalName != null) {
                Literal literal = signalToLiteralMap.computeIfAbsent(literalName, Literal::new);
                variables.add(contact);
                values.add(literal);
            }
        }
        for (FunctionContact contact : component.getFunctionContacts()) {
            if (contact.isOutput()) {
                BooleanFormula setFunction = FormulaUtils.replace(contact.getSetFunction(), variables, values);
                BooleanFormula resetFunction = FormulaUtils.replace(contact.getResetFunction(), variables, values);
                SignalInfo signalInfo = new SignalInfo(contact, setFunction, resetFunction);
                result.add(signalInfo);
            }
        }
        return result;
    }

    public String getComponentFlattenReference(FunctionComponent component) {
        return NamespaceHelper.flattenReference(circuit.getComponentReference(component));
    }

    public Set<Integer> getBusIndexes(String busName) {
        return busIndexesMap.get(busName);
    }

    public void removeBus(String busName) {
        busIndexesMap.remove(busName);
    }

}
