package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.tools.InitialisationAnalyserTool;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ResetUtils {

    private static final BooleanWorker DUMB_WORKER = DumbBooleanWorker.getInstance();
    private static final BooleanWorker CLEVER_WORKER = CleverBooleanWorker.getInstance();
    private static final String VERIFICATION_RESULT_TITLE = "Verification result";

    private ResetUtils() {
    }

    public static Set<Contact> tagForcedInitClearAll(Circuit circuit) {
        return setForcedInit(circuit.getFunctionContacts(), false);
    }

    public static Set<Contact> tagForcedInitInputPorts(Circuit circuit) {
        return setForcedInit(circuit.getInputPorts(), true);
    }

    public static Set<Contact> tagForcedInitProblematicPins(Circuit circuit) {
        return setForcedInit(getProblematicPins(circuit), true);
    }

    public static Set<Contact> getProblematicPins(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact outputContact : component.getFunctionOutputs()) {
                if (outputContact.getForcedInit()) continue;
                LinkedList<BooleanVariable> variables = new LinkedList<>();
                LinkedList<BooleanFormula> values = new LinkedList<>();
                for (FunctionContact contact : component.getFunctionContacts()) {
                    if (contact == outputContact) continue;
                    Pair<Contact, Boolean> pair = CircuitUtils.findDriverAndInversionSkipZeroDelay(circuit, contact);
                    if (pair == null) continue;
                    Contact driver = pair.getFirst();
                    if ((driver != null) && (driver != outputContact)) {
                        variables.add(contact);
                        boolean inverting = pair.getSecond();
                        BooleanFormula value = (driver.getInitToOne() != inverting) ? One.getInstance() : Zero.getInstance();
                        values.add(value);
                    }
                }
                if (isProblematicPin(outputContact, variables, values)) {
                    result.add(outputContact);
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
        if ((setFunction == null) && (resetFunction == null)) {
            return false;
        }
        return (!isEvaluatedHigh(setFunction, resetFunction) || !contact.getInitToOne())
                && (!isEvaluatedLow(setFunction, resetFunction) || contact.getInitToOne());
    }

    public static Set<Contact> tagForcedInitSequentialPins(Circuit circuit) {
        Set<FunctionContact> contacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionOutputs()) {
                if ((contact.getSetFunction() == null) || (contact.getResetFunction() == null)) continue;
                contacts.add(contact);
            }
        }
        return setForcedInit(contacts, true);
    }

    public static boolean isEvaluatedHigh(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return One.getInstance().equals(setFunction) && ((resetFunction == null) || Zero.getInstance().equals(resetFunction));
    }


    public static boolean isEvaluatedLow(BooleanFormula setFunction, BooleanFormula resetFunction) {
        return Zero.getInstance().equals(setFunction) && ((resetFunction == null) || One.getInstance().equals(resetFunction));
    }

    public static Set<Contact> tagForcedInitAutoAppend(Circuit circuit) {
        Set<Contact> contacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay() && !component.getAvoidInit()) {
                for (FunctionContact contact : component.getFunctionContacts()) {
                    if (contact.isPin() && contact.isDriver()) {
                        contacts.add(contact);
                    }
                }
            }
        }
        Set<Contact> changedContacts = setForcedInit(contacts, true);
        Set<Contact> redundantContacts = simplifyForcedInit(circuit, changedContacts);
        changedContacts.removeAll(redundantContacts);
        return changedContacts;
    }

    private static Set<Contact> setForcedInit(Collection<? extends Contact> contacts, boolean value) {
        Set<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
            if (contact.getForcedInit() != value) {
                contact.setForcedInit(value);
                result.add(contact);
            }
        }
        return result;
    }

    private static Set<Contact> simplifyForcedInit(Circuit circuit, Collection<? extends Contact> contacts) {
        Set<Contact> result = new HashSet<>();
        for (Contact contact : orderContactsByHeuristic(contacts)) {
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

    private static List<Contact> orderContactsByHeuristic(Collection<? extends Contact> contacts) {
        int tierCount = 6;
        List<List<Contact>> tiers = new ArrayList<>(tierCount);
        for (int tierIndex = 0; tierIndex < tierCount; tierIndex++) {
            tiers.add(new ArrayList<>());
        }
        for (Contact contact : contacts) {
            int tierIndex = tierCount - 1;
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                FunctionComponent component = (FunctionComponent) parent;
                if (component.isSequentialGate()) {
                    tierIndex = 0;
                } else if (component.getIsArbitrationPrimitive()) {
                    tierIndex = 1;
                } else if (component.getInputs().size() > 2) {
                    tierIndex = 2;
                } else if (component.getInputs().size() > 1) {
                    tierIndex = 3;
                } else {
                    tierIndex = 4;
                }
            }
            List<Contact> tier = tiers.get(Math.min(tierIndex, tierCount - 1));
            tier.add(contact);
        }
        return tiers.stream().flatMap(List::stream).toList();
    }

    public static Set<Contact> tagForcedInitAutoDiscard(Circuit circuit) {
        Set<FunctionContact> contacts = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver() && contact.getForcedInit()) {
                contacts.add(contact);
            }
        }
        return simplifyForcedInit(circuit, contacts);
    }

    public static boolean insertReset(VisualCircuit circuit, boolean isActiveLow) {
        String portName = isActiveLow ? CircuitSettings.getResetActiveLowPort() : CircuitSettings.getResetActiveHighPort();
        List<VisualFunctionComponent> resetComponents = circuit.getVisualFunctionComponents().stream()
                .filter(component -> hasForcedInitOutput(component.getReferencedComponent())
                        || (CircuitUtils.getFunctionContact(circuit, component, portName) != null))
                .toList();

        if (resetComponents.isEmpty()) {
            return circuit.getVisualComponentByMathReference(portName, VisualContact.class) != null;
        }

        VisualFunctionContact resetPort = CircuitUtils.getOrCreatePort(circuit, portName,
                Contact.IOType.INPUT, VisualContact.Direction.WEST);

        if (resetPort == null) {
            return false;
        }

        boolean clearMapping = circuit.getVisualFunctionComponents().stream()
                .noneMatch(VisualFunctionComponent::isMapped);

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
        CircuitUtils.detachJoint(circuit, resetPort, 0.5);
        setInitialisationProtocol(circuit, resetPort, isActiveLow);
        return true;
    }

    public static boolean hasForcedInitOutput(CircuitComponent component) {
        for (Contact outputContact : component.getOutputs()) {
            if (outputContact.getForcedInit()) {
                return true;
            }
        }
        return false;
    }

    private static void connectIfPossible(VisualCircuit circuit, VisualContact fromContact, VisualContact toContact) {
        if ((fromContact != null) && (toContact != null)) {
            VisualConnection connection = circuit.getConnection(fromContact, toContact);
            if (connection == null) {
                try {
                    circuit.connect(fromContact, toContact);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
        }
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
        circuit.setMathName(outputContact, Identifier.getTemporaryName());
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
        circuit.setMathName(outputContact, Identifier.getTemporaryName());
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

    private static Collection<VisualFunctionComponent> resetComponent(VisualCircuit circuit,
            VisualFunctionComponent component, VisualContact resetPort, boolean isActiveLow, boolean clearMapping) {

        return component.isMapped()
            ? resetMappedComponent(circuit, component, resetPort, isActiveLow)
            : resetUnmappedComponent(circuit, component, resetPort, isActiveLow, clearMapping);
    }

    private static Collection<VisualFunctionComponent> resetMappedComponent(VisualCircuit circuit,
            VisualFunctionComponent component, VisualContact resetPort, boolean isActiveLow) {

        Collection<VisualFunctionContact> initLowOutputs = new HashSet<>();
        Collection<VisualFunctionContact> initHighOutputs = new HashSet<>();
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            if (contact.isOutput() && contact.isPin() && contact.getForcedInit()) {
                if (contact.getInitToOne()) {
                    initHighOutputs.add(contact);
                } else {
                    initLowOutputs.add(contact);
                }
            }
        }

        Collection<VisualFunctionComponent> result = new HashSet<>();
        String moduleName = component.getReferencedComponent().getModule();
        Pair<String, String> initLowGatePinPair = CircuitSettings.getInitLowGatePinPair(moduleName);
        Pair<String, String> initHighGatePinPair = CircuitSettings.getInitHighGatePinPair(moduleName);
        if ((initLowGatePinPair == null) && (initHighGatePinPair == null)) {
            result.addAll(resetByAddingGate(circuit, component, initLowOutputs, resetPort, isActiveLow, false));
            result.addAll(resetByAddingGate(circuit, component, initHighOutputs, resetPort, isActiveLow, false));
        } else {
            if ((initLowGatePinPair != null) && ((initLowOutputs.size() > initHighOutputs.size()) || (initHighGatePinPair == null))) {
                result.addAll(resetByReplacingGate(circuit, component, initLowOutputs, resetPort, isActiveLow, initLowGatePinPair));
                result.addAll(resetByAddingGate(circuit, component, initHighOutputs, resetPort, isActiveLow, false));
            } else {
                result.addAll(resetByReplacingGate(circuit, component, initHighOutputs, resetPort, isActiveLow, initHighGatePinPair));
                result.addAll(resetByAddingGate(circuit, component, initLowOutputs, resetPort, isActiveLow, false));
            }
        }
        return result;
    }

    private static Collection<VisualFunctionComponent> resetUnmappedComponent(VisualCircuit circuit,
            VisualFunctionComponent component, VisualContact resetPort, boolean isActiveLow, boolean clearMapping) {

        Collection<VisualFunctionComponent> result = new HashSet<>();
        Collection<VisualFunctionContact> combInitOutputs = new HashSet<>();
        Collection<VisualFunctionContact> seqInitLowOutputs = new HashSet<>();
        Collection<VisualFunctionContact> seqInitHighOutputs = new HashSet<>();
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            if (contact.isOutput() && contact.isPin() && contact.getForcedInit()) {
                if (contact.getReferencedComponent().isSequential()) {
                    if (contact.getInitToOne()) {
                        seqInitHighOutputs.add(contact);
                    } else {
                        seqInitLowOutputs.add(contact);
                    }
                } else {
                    combInitOutputs.add(contact);
                }
            }
        }

        result.addAll(resetByAddingGate(circuit, component, combInitOutputs, resetPort, isActiveLow, clearMapping));
        result.addAll(resetByReplacingGate(circuit, component, seqInitLowOutputs, resetPort, isActiveLow, Pair.of("", "R")));
        result.addAll(resetByReplacingGate(circuit, component, seqInitHighOutputs, resetPort, isActiveLow, Pair.of("", "S")));
        return result;
    }

    private static Collection<VisualFunctionComponent> resetByAddingGate(VisualCircuit circuit,
            VisualFunctionComponent component, Collection<VisualFunctionContact> forcedInitOutputs,
            VisualContact resetPort, boolean isActiveLow, boolean clearMapping) {

        Collection<VisualFunctionComponent> result = new HashSet<>();
        for (VisualFunctionContact contact : forcedInitOutputs) {
            VisualFunctionComponent resetGate = insertResetGate(circuit, resetPort, contact, isActiveLow);
            result.add(resetGate);
            if (clearMapping) {
                resetGate.clearMapping();
            }
        }
        GateUtils.propagateInitialState(circuit, component, forcedInitOutputs);
        return result;
    }

    private static Collection<VisualFunctionComponent> resetByReplacingGate(VisualCircuit circuit,
            VisualFunctionComponent component, Collection<VisualFunctionContact> forcedInitOutputs,
            VisualContact resetPort, boolean isActiveLow, Pair<String, String> initGatePinPair) {

        Collection<VisualFunctionComponent> result = new HashSet<>();
        if (!forcedInitOutputs.isEmpty()) {
            VisualFunctionContact initContact = getOrCreateResetPin(circuit, component, initGatePinPair.getSecond());
            connectIfPossible(circuit, resetPort, initContact);
            for (VisualFunctionContact contact : forcedInitOutputs) {
                insertResetFunction(contact, initContact, isActiveLow);
            }
            component.getReferencedComponent().setModule(initGatePinPair.getFirst());
            result.add(component);
        }
        return result;
    }

    private static VisualFunctionContact getOrCreateResetPin(VisualCircuit circuit, VisualFunctionComponent component, String name) {
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

        SpaceUtils.makeSpaceAroundContact(circuit, contact, 3.0);
        VisualFunctionComponent resetGate = createResetGate(circuit, contact.getInitToOne(), activeLow);
        GateUtils.insertGateAfter(circuit, resetGate, contact, 2.0);
        connectHangingInputs(circuit, resetPort, resetGate);
        GateUtils.propagateInitialState(circuit, resetGate);
        return resetGate;
    }

    private static VisualFunctionComponent createResetGate(VisualCircuit circuit, boolean initToOne, boolean activeLow) {
        if (activeLow) {
            return initToOne ? GateUtils.createNand2bGate(circuit) : GateUtils.createAnd2Gate(circuit);
        } else {
            return initToOne ? GateUtils.createOr2Gate(circuit) : GateUtils.createNor2bGate(circuit);
        }
    }

    private static void connectHangingInputs(VisualCircuit circuit, VisualContact port, VisualFunctionComponent component) {
        for (VisualContact contact : component.getVisualInputs()) {
            if (circuit.getPreset(contact).isEmpty()) {
                connectIfPossible(circuit, port, contact);
            }
        }
    }

    private static void setInitialisationProtocol(VisualCircuit circuit, VisualFunctionContact resetPort, boolean activeLow) {
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

    public static Boolean checkInitialisationViaForcedInputPorts(Circuit circuit, boolean useAnalysisTool) {
        if (!VerificationUtils.checkBlackboxComponents(circuit)) {
            return null;
        }

        InitialisationState initState = new InitialisationState(circuit);
        Collection<Contact> forcedPorts = initState.getForcedPorts();
        List<String> forcedPortRefs = ReferenceHelper.getReferenceList(circuit, forcedPorts);
        SortUtils.sortNatural(forcedPortRefs);
        String forcedPortText = "\n\n" + PropertyHelper.BULLET_PREFIX + (forcedPortRefs.isEmpty()
                ? "No forced input ports"
                : TextUtils.wrapMessageWithItems("Forced input port", forcedPortRefs));

        List<String> uninitialisedPinRefs = new ArrayList<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay() && !component.getAvoidInit()) {
                for (FunctionContact contact : component.getFunctionContacts()) {
                    if (contact.isOutput() && (!initState.isInitialisedPin(contact) || contact.getForcedInit())) {
                        String contactRef = circuit.getNodeReference(contact);
                        uninitialisedPinRefs.add(contactRef);
                    }
                }
            }
        }

        if (uninitialisedPinRefs.isEmpty()) {
            DialogUtils.showInfo("Circuit is fully initialised via forced input ports."
                    + forcedPortText, VERIFICATION_RESULT_TITLE);
            return true;
        }

        if (useAnalysisTool) {
            Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()) {
                Toolbox toolbox = framework.getMainWindow().getCurrentToolbox();
                toolbox.selectTool(toolbox.getToolInstance(InitialisationAnalyserTool.class));
            }
        }

        SortUtils.sortNatural(uninitialisedPinRefs);
        String uninitialisedPinText = "\n\n" + PropertyHelper.BULLET_PREFIX +
                TextUtils.wrapMessageWithItems("Uninitialised signal", uninitialisedPinRefs);

        DialogUtils.showError("Circuit is not fully initialised via forced input ports."
                + forcedPortText + uninitialisedPinText, VERIFICATION_RESULT_TITLE);

        return false;
    }

    public static boolean isResetInputPortName(String portName) {
        Set<String> names = Stream.of(CircuitSettings.getResetActiveLowPort(), CircuitSettings.getResetActiveHighPort())
                .filter(Objects::nonNull).collect(Collectors.toSet());

        return names.contains(portName);
    }

}
