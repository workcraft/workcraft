package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.types.Pair;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;
import java.util.stream.Collectors;

public final class ResetUtils {

    private static final BooleanWorker DUMB_WORKER = DumbBooleanWorker.getInstance();
    private static final BooleanWorker CLEVER_WORKER = CleverBooleanWorker.getInstance();

    private ResetUtils() {
    }

    public static Set<Contact> tagForceInitClearAll(Circuit circuit) {
        return setForceInit(circuit.getFunctionContacts(), false);
    }

    public static Set<Contact> tagForceInitInputPorts(Circuit circuit) {
        return setForceInit(circuit.getInputPorts(), true);
    }

    public static Set<Contact> tagForceInitProblematicPins(Circuit circuit) {
        return setForceInit(getProblematicPins(circuit), true);
    }

    public static Set<Contact> getProblematicPins(Circuit circuit) {
        HashSet<Contact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact outputContact : component.getFunctionOutputs()) {
                if (!outputContact.getForcedInit()) {
                    LinkedList<BooleanVariable> variables = new LinkedList<>();
                    LinkedList<BooleanFormula> values = new LinkedList<>();
                    for (FunctionContact contact : component.getFunctionContacts()) {
                        Pair<Contact, Boolean> pair = CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit, contact);
                        if (pair != null) {
                            Contact driver = pair.getFirst();
                            if ((driver != null) && (driver != outputContact)) {
                                variables.add(contact);
                                boolean inverting = pair.getSecond();
                                BooleanFormula value = (driver.getInitToOne() == inverting) ? Zero.getInstance() : One.getInstance();
                                values.add(value);
                            }
                        }
                    }
                    if (isProblematicPin(outputContact, variables, values)) {
                        result.add(outputContact);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isProblematicPin(FunctionContact contact,
            LinkedList<BooleanVariable> variables, LinkedList<BooleanFormula> values) {

        if (contact.getForcedInit()) {
            return false;
        }
        BooleanFormula setFunction = FormulaUtils.replace(contact.getSetFunction(), variables, values, CLEVER_WORKER);
        BooleanFormula resetFunction = FormulaUtils.replace(contact.getResetFunction(), variables, values, CLEVER_WORKER);
        if (isEvaluatedHigh(setFunction, resetFunction) && contact.getInitToOne()) {
            return false;
        }
        return !isEvaluatedLow(setFunction, resetFunction) || contact.getInitToOne();
    }

    public static Set<Contact> tagForceInitSequentialPins(Circuit circuit) {
        HashSet<FunctionContact> contacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionOutputs()) {
                if ((contact.getSetFunction() == null) || (contact.getResetFunction() == null)) continue;
                contacts.add(contact);
            }
        }
        return setForceInit(contacts, true);
    }

    public static boolean isEvaluatedHigh(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return One.getInstance().equals(setFunction) && ((resetFunction == null) || Zero.getInstance().equals(resetFunction));
    }


    public static boolean isEvaluatedLow(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return Zero.getInstance().equals(setFunction) && ((resetFunction == null) || One.getInstance().equals(resetFunction));
    }

    public static Set<Contact> tagForceInitAutoAppend(Circuit circuit) {
        Set<Contact> contacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay()) {
                for (FunctionContact contact : component.getFunctionContacts()) {
                    if (contact.isPin() && contact.isDriver()) {
                        contacts.add(contact);
                    }
                }
            }
        }
        Set<Contact> changedContacts = setForceInit(contacts, true);
        Set<Contact> redundantContacts = simplifyForceInit(circuit, changedContacts);
        changedContacts.removeAll(redundantContacts);
        return changedContacts;
    }

    private static Set<Contact> setForceInit(Collection<? extends Contact> contacts, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
            if (contact.getForcedInit() != value) {
                contact.setForcedInit(value);
                result.add(contact);
            }
        }
        return result;
    }

    private static Set<Contact> simplifyForceInit(Circuit circuit, Collection<? extends Contact> contacts) {
        Set<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
            contact.setForcedInit(false);
            InitialisationState initState = new InitialisationState(circuit);
            if (initState.isInitialisedPin(contact)) {
                result.add(contact);
            } else {
                contact.setForcedInit(true);
            }
        }
        return result;
    }

    public static Set<Contact> tagForceInitAutoDiscard(Circuit circuit) {
        HashSet<FunctionContact> contacts = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver() && contact.getForcedInit()) {
                contacts.add(contact);
            }
        }
        return simplifyForceInit(circuit, contacts);
    }

    public static boolean insertReset(WorkspaceEntry we, boolean isActiveLow) {
        boolean result = false;
        if (WorkspaceUtils.isApplicable(we, VisualCircuit.class)) {
            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            we.captureMemento();
            result = insertReset(circuit, isActiveLow);
        }
        if (result) {
            we.saveMemento();
        } else {
            we.uncaptureMemento();
        }
        return result;
    }

    public static boolean insertReset(VisualCircuit circuit, boolean isActiveLow) {
        String portName = isActiveLow ? CircuitSettings.getResetActiveLowPort() : CircuitSettings.getResetActiveHighPort();
        List<VisualFunctionComponent> resetComponents = circuit.getVisualFunctionComponents().stream()
                .filter(component -> hasForceInitOutput(component.getReferencedComponent())
                        || (CircuitUtils.getFunctionContact(circuit, component, portName) != null))
                .collect(Collectors.toList());

        VisualFunctionContact resetPort = resetComponents.isEmpty() ? null
                : CircuitUtils.getOrCreatePort(circuit, portName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

        if (resetPort != null) {
            boolean clearMapping = !hasMappedComponents(circuit);
            for (VisualFunctionComponent component : resetComponents) {
                if (component.isBuffer()) {
                    resetBuffer(circuit, component, resetPort, isActiveLow, clearMapping);
                } else if (component.isInverter()) {
                    resetInverter(circuit, component, resetPort, isActiveLow, clearMapping);
                } else {
                    VisualFunctionContact resetContact = CircuitUtils.getFunctionContact(circuit, component, portName);
                    if (resetContact == null) {
                        resetComponent(circuit, component, resetPort, isActiveLow, clearMapping);
                    } else {
                        connectIfPossible(circuit, resetPort, resetContact);
                    }
                }
            }
            SpaceUtils.positionPort(circuit, resetPort, false);
            forceInitResetCircuit(circuit, resetPort, isActiveLow);
        }
        return !resetComponents.isEmpty();
    }

    public static boolean hasForceInitOutput(CircuitComponent component) {
        for (Contact outputContact : component.getOutputs()) {
            if (outputContact.getForcedInit()) {
                return true;
            }
        }
        return false;
    }

    private static void connectIfPossible(VisualCircuit circuit, VisualContact fromContact, VisualContact toContact) {
        if ((fromContact != null) && (toContact != null)) {
            try {
                circuit.connect(fromContact, toContact);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean hasMappedComponents(VisualCircuit circuit) {
        boolean hasMappedComponent = false;
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            hasMappedComponent |= component.isMapped();
        }
        return hasMappedComponent;
    }

    private static VisualFunctionComponent resetBuffer(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact resetPort, boolean activeLow, boolean clearMapping) {

        VisualFunctionContact outputContact = component.getFirstVisualOutput();
        if ((outputContact == null) || !outputContact.getForcedInit()) {
            return null;
        }

        String gateName = "";
        String in1Name = "AN";
        String in2Name = "B";
        String outName = "ON";

        FreeVariable in1Var = new FreeVariable(in1Name);
        FreeVariable in2Var = new FreeVariable(in2Name);
        BooleanFormula formula = getResetBufferFormula(activeLow, outputContact.getInitToOne(), in1Var, in2Var);
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(formula, LibraryManager.getLibrary());
        if (mapping != null) {
            Gate gate = mapping.getFirst();
            gateName = clearMapping ? "" : gate.name;
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            in1Name = assignments.get(in1Var);
            in2Name = assignments.get(in2Var);
            outName = gate.function.name;
        }

        VisualFunctionContact inputContact = component.getFirstVisualInput();
        // Temporary rename gate output, so there is no name clash on renaming gate input
        circuit.setMathName(outputContact, Identifier.makeInternal(outName));
        circuit.setMathName(inputContact, in1Name);
        circuit.setMathName(outputContact, outName);
        VisualFunctionContact resetContact = circuit.getOrCreateContact(component, in2Name, Contact.IOType.INPUT);
        connectIfPossible(circuit, resetPort, resetContact);

        Contact in1Contact = inputContact.getReferencedComponent();
        Contact in2Contact = resetContact.getReferencedComponent();
        BooleanFormula setFunction = FormulaUtils.replace(formula, Arrays.asList(in1Var, in2Var),
                Arrays.asList(in1Contact, in2Contact));

        outputContact.setSetFunction(setFunction);
        component.setLabel(gateName);
        return component;
    }

    private static BooleanFormula getResetBufferFormula(boolean activeLow, boolean initToOne,
            BooleanVariable in1Var, BooleanVariable in2Var) {

        if (initToOne) {
            if (activeLow) {
                return new Not(new And(new Not(in1Var), in2Var));
            } else {
                return new Or(in1Var, in2Var);
            }
        } else {
            if (activeLow) {
                return new And(in1Var, in2Var);
            } else {
                return new Not(new Or(new Not(in1Var), in2Var));
            }
        }
    }

    private static VisualFunctionComponent resetInverter(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact resetPort, boolean activeLow, boolean clearMapping) {

        VisualFunctionContact outputContact = component.getFirstVisualOutput();
        if ((outputContact == null) || !outputContact.getForcedInit()) {
            return null;
        }

        String gateName = "";
        String in1Name = "AN";
        String in2Name = "B";
        String outName = "ON";

        FreeVariable in1Var = new FreeVariable(in1Name);
        FreeVariable in2Var = new FreeVariable(in2Name);
        BooleanFormula formula = getResetInverterFormula(activeLow, outputContact.getInitToOne(), in1Var, in2Var);
        Pair<Gate, Map<BooleanVariable, String>> mapping = GenlibUtils.findMapping(formula, LibraryManager.getLibrary());
        if (mapping != null) {
            Gate gate = mapping.getFirst();
            gateName = clearMapping ? "" : gate.name;
            Map<BooleanVariable, String> assignments = mapping.getSecond();
            in1Name = assignments.get(in1Var);
            in2Name = assignments.get(in2Var);
            outName = gate.function.name;
        }

        VisualFunctionContact inputContact = component.getFirstVisualInput();
        // Temporary rename gate output, so there is no name clash on renaming gate input
        circuit.setMathName(outputContact, Identifier.makeInternal(outName));
        circuit.setMathName(inputContact, in2Name);
        circuit.setMathName(outputContact, outName);
        VisualFunctionContact resetContact = circuit.getOrCreateContact(component, in1Name, Contact.IOType.INPUT);
        connectIfPossible(circuit, resetPort, resetContact);

        Contact in1Contact = resetContact.getReferencedComponent();
        Contact in2Contact = inputContact.getReferencedComponent();
        BooleanFormula setFunction = FormulaUtils.replace(formula, Arrays.asList(in1Var, in2Var),
                Arrays.asList(in1Contact, in2Contact));

        outputContact.setSetFunction(setFunction);
        component.setLabel(gateName);
        return component;
    }

    private static BooleanFormula getResetInverterFormula(boolean activeLow, boolean initToOne,
            BooleanVariable in1Var, BooleanVariable in2Var) {

        if (initToOne) {
            if (activeLow) {
                return new Not(new And(in1Var, in2Var));
            } else {
                return new Not(new And(new Not(in1Var), in2Var));
            }
        } else {
            if (activeLow) {
                return new Not(new Or(new Not(in1Var), in2Var));
            } else {
                return new Not(new Or(in1Var, in2Var));
            }
        }
    }

    private static Collection<VisualFunctionComponent> resetComponent(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact resetPort, boolean isActiveLow, boolean clearMapping) {

        Collection<VisualFunctionComponent> result = new HashSet<>();

        boolean isSimpleGate = component.isGate() && (component.getVisualInputs().size() < 3);
        Collection<VisualFunctionContact> forceInitGateContacts = new HashSet<>();
        Collection<VisualFunctionContact> forceInitFuncContacts = new HashSet<>();
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            if (contact.isOutput() && contact.isPin() && contact.getForcedInit()) {
                if (isSimpleGate || contact.getReferencedComponent().isSequential()) {
                    forceInitFuncContacts.add(contact);
                } else {
                    forceInitGateContacts.add(contact);
                }
            }
        }
        if (!forceInitFuncContacts.isEmpty()) {
            VisualFunctionContact setContact = null;
            VisualFunctionContact clearContact = null;
            for (VisualFunctionContact contact : forceInitFuncContacts) {
                if (contact.getInitToOne()) {
                    setContact = getOrCreatePin(circuit, component, CircuitSettings.getSetPin());
                    insertResetFunction(contact, setContact, isActiveLow);
                } else {
                    clearContact = getOrCreatePin(circuit, component, CircuitSettings.getClearPin());
                    insertResetFunction(contact, clearContact, isActiveLow);
                }
            }
            // Modify module name by adding set/clear suffix
            String moduleName = component.getReferencedComponent().getModule();
            if (!moduleName.isEmpty()) {
                if (setContact != null) {
                    moduleName += CircuitSettings.getSetPin();
                }
                if (clearContact != null) {
                    moduleName += CircuitSettings.getClearPin();
                }
                component.getReferencedComponent().setModule(moduleName);
            }
            // Connect set/clear pins to reset port
            connectIfPossible(circuit, resetPort, setContact);
            connectIfPossible(circuit, resetPort, clearContact);
            result.add(component);
        }

        for (VisualFunctionContact contact : forceInitGateContacts) {
            VisualFunctionComponent resetGate = insertResetGate(circuit, resetPort, contact, isActiveLow);
            result.add(resetGate);
            if (clearMapping) {
                resetGate.clearMapping();
            }
        }
        return result;
    }

    private static VisualFunctionContact getOrCreatePin(VisualCircuit circuit, VisualFunctionComponent component, String name) {
        String ref = NamespaceHelper.getReference(circuit.getMathReference(component), name);
        VisualFunctionContact result = circuit.getVisualComponentByMathReference(ref, VisualFunctionContact.class);
        if (result == null) {
            result = circuit.getOrCreateContact(component, name, Contact.IOType.INPUT);
            component.setPositionByDirection(result, VisualContact.Direction.WEST, false);
        }
        return result;
    }

    private static void insertResetFunction(VisualFunctionContact contact, VisualContact resetContact, boolean activeLow) {
        BooleanFormula setFunction = contact.getSetFunction();
        BooleanFormula resetFunction = contact.getResetFunction();
        Contact resetVar = resetContact.getReferencedComponent();
        if (activeLow) {
            if (contact.getInitToOne()) {
                if (setFunction != null) {
                    contact.setSetFunction(DUMB_WORKER.or(DUMB_WORKER.not(resetVar), setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(DUMB_WORKER.and(resetVar, resetFunction));
                }
            } else {
                if (setFunction != null) {
                    contact.setSetFunction(DUMB_WORKER.and(resetVar, setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(DUMB_WORKER.or(DUMB_WORKER.not(resetVar), resetFunction));
                }
            }
        } else {
            if (contact.getInitToOne()) {
                if (setFunction != null) {
                    contact.setSetFunction(DUMB_WORKER.or(resetVar, setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(DUMB_WORKER.and(DUMB_WORKER.not(resetVar), resetFunction));
                }
            } else {
                if (setFunction != null) {
                    contact.setSetFunction(DUMB_WORKER.and(DUMB_WORKER.not(resetVar), setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(DUMB_WORKER.or(resetVar, resetFunction));
                }
            }
        }
    }

    private static VisualFunctionComponent insertResetGate(VisualCircuit circuit, VisualContact resetPort,
            VisualFunctionContact contact, boolean activeLow) {

        SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
        VisualFunctionComponent resetGate = createResetGate(circuit, contact.getInitToOne(), activeLow);
        GateUtils.insertGateAfter(circuit, resetGate, contact);
        connectHangingInputs(circuit, resetPort, resetGate);
        GateUtils.propagateInitialState(circuit, resetGate);
        return resetGate;
    }

    private static VisualFunctionComponent createResetGate(VisualCircuit circuit, boolean initToOne, boolean activeLow) {
        if (activeLow) {
            return initToOne ? GateUtils.createNandbGate(circuit) : GateUtils.createAndGate(circuit);
        } else {
            return initToOne ? GateUtils.createOrGate(circuit) : GateUtils.createNorbGate(circuit);
        }
    }

    private static void connectHangingInputs(VisualCircuit circuit, VisualContact port, VisualFunctionComponent component) {
        for (VisualContact contact : component.getVisualInputs()) {
            if (circuit.getPreset(contact).isEmpty()) {
                connectIfPossible(circuit, port, contact);
            }
        }
    }

    private static void forceInitResetCircuit(VisualCircuit circuit, VisualFunctionContact resetPort, boolean activeLow) {
        resetPort.setInitToOne(!activeLow);
        resetPort.setForcedInit(true);
        resetPort.setSetFunction(activeLow ? One.getInstance() : Zero.getInstance());
        resetPort.setResetFunction(activeLow ? Zero.getInstance() : One.getInstance());
        for (VisualFunctionContact contact : circuit.getVisualFunctionContacts()) {
            if (contact.isPin() && contact.isOutput()) {
                contact.setForcedInit(false);
            }
        }
    }

    public static Set<Contact> getInitialisationProblemContacts(Circuit circuit) {
        InitialisationState initState = new InitialisationState(circuit);
        Set<Contact> result = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver()) {
                if (!initState.isInitialisedPin(contact)) {
                    result.add(contact);
                }
            }
        }
        return result;
    }

}
