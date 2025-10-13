package org.workcraft.plugins.circuit.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.Gate;
import org.workcraft.plugins.circuit.genlib.GenlibUtils;
import org.workcraft.plugins.circuit.genlib.LibraryManager;
import org.workcraft.plugins.circuit.tools.InitialisationAnalyserTool;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.*;

public final class ResetUtils {

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
            if (parent instanceof FunctionComponent component) {
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
        String portName = isActiveLow
                ? CircuitSettings.getResetActiveLowPort()
                : CircuitSettings.getResetActiveHighPort();

        List<VisualFunctionComponent> resetComponents = circuit.getVisualFunctionComponents().stream()
                .filter(component -> hasForcedInitOutput(component.getReferencedComponent())
                        || (CircuitUtils.getFunctionContact(circuit, component, portName) != null))
                .toList();

        VisualFunctionContact resetPort = resetComponents.isEmpty() ? null
                : CircuitUtils.getOrCreatePort(circuit, portName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

        if (resetPort != null) {
            for (VisualFunctionComponent component : resetComponents) {
                insertReset(circuit, component, resetPort, isActiveLow);
            }
            SpaceUtils.positionPortAtBottom(circuit, resetPort, false);
            setInitialisationProtocol(circuit, resetPort, isActiveLow);
        }
        return resetPort != null;
    }

    private static boolean hasForcedInitOutput(CircuitComponent component) {
        return component.getOutputs().stream().anyMatch(Contact::getForcedInit);
    }

    private static void insertReset(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact resetPort, boolean isActiveLow) {

        // Attempt to reset mapped gate preserving mapping
        if (resetMappedCombinationalGateIfPossible(circuit, component, resetPort, isActiveLow)) {
            return;
        }

        String portName = isActiveLow
                ? CircuitSettings.getResetActiveLowPort()
                : CircuitSettings.getResetActiveHighPort();

        // Attempt to reuse existing reset pin
        VisualFunctionContact resetInputPin = CircuitUtils.getFunctionContact(circuit, component, portName);
        if (resetInputPin != null) {
            CircuitUtils.connectIfPossible(circuit, resetPort, resetInputPin);
            ConversionUtils.replicateDriverContact(circuit, resetInputPin);
            return;
        }

        // Fall back to resetting mapped or unmapped component
        if (component.isMapped()) {
            resetMappedComponent(circuit, component, resetPort, isActiveLow);
        } else {
            resetUnmappedComponent(circuit, component, resetPort, isActiveLow);
        }
    }

    private static boolean resetMappedCombinationalGateIfPossible(VisualCircuit circuit,
            VisualFunctionComponent component, VisualFunctionContact resetPort, boolean activeLow) {

        if (!component.isMapped() || !component.isCombinationalGate()) {
            return false;
        }
        VisualFunctionContact outputPin = component.getGateOutput();
        if (outputPin == null) {
            return false;
        }
        BooleanFormula setFunction = outputPin.getSetFunction();
        if (setFunction == null) {
            return false;
        }
        FunctionContact resetVar = new FunctionContact(Contact.IOType.INPUT);
        BooleanFormula formulaWithReset = getFormulaWithReset(setFunction, resetVar, activeLow, outputPin.getInitToOne());
        Triple<Gate, Map<BooleanVariable, String>, Set<String>> extendedMapping = GenlibUtils.findExtendedMapping(
                formulaWithReset, LibraryManager.getLibrary(), true, true);

        if (extendedMapping == null) {
            return false;
        }
        // Add reset pin and update gate function
        VisualFunctionContact resetInputPin = new VisualFunctionContact(resetVar);
        component.addContact(resetInputPin);
        outputPin.setSetFunction(formulaWithReset);
        // Connect reset pin to reset port proxy
        CircuitUtils.connectIfPossible(circuit, resetPort, resetInputPin);
        ConversionUtils.replicateDriverContact(circuit, resetInputPin);
        // Convert the gate according to the extended mapping data
        GateUtils.convertGate(circuit, component, extendedMapping);
        return true;
    }

    private static BooleanFormula getFormulaWithReset(BooleanFormula formula, BooleanVariable initVar,
            boolean activeLow, boolean initToOne) {

        if (formula == null) {
            return null;
        }
        if (initToOne) {
            if (activeLow) {
                if (formula instanceof One) {
                    return One.getInstance();
                } else if (formula instanceof Zero) {
                    return new Not(initVar);
                } else if (formula instanceof Not notFormula) {
                    return new Not(new And(notFormula.getX(), initVar));
                } else {
                    return new Or(formula, new Not(initVar));
                }
            } else {
                if (formula instanceof One) {
                    return One.getInstance();
                } else if (formula instanceof Zero) {
                    return initVar;
                } else if (formula instanceof Not notFormula) {
                    return new Not(new And(notFormula.getX(), new Not(initVar)));
                } else {
                    return new Or(formula, initVar);
                }
            }
        } else {
            if (activeLow) {
                if (formula instanceof Zero) {
                    return Zero.getInstance();
                } else if (formula instanceof One) {
                    return initVar;
                } else if (formula instanceof Not notFormula) {
                    return new Not(new Or(notFormula.getX(), new Not(initVar)));
                } else {
                    return new And(formula, initVar);
                }
            } else {
                if (formula instanceof Zero) {
                    return Zero.getInstance();
                } else if (formula instanceof One) {
                    return new Not(initVar);
                } else if (formula instanceof Not notFormula) {
                    return new Not(new Or(notFormula.getX(), initVar));
                } else {
                    return new And(formula, new Not(initVar));
                }
            }
        }
    }

    private static void resetMappedComponent(VisualCircuit circuit,
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

        String moduleName = component.getReferencedComponent().getModule();
        Pair<String, String> initLowGatePinPair = CircuitSettings.getInitLowGatePinPair(moduleName);
        Pair<String, String> initHighGatePinPair = CircuitSettings.getInitHighGatePinPair(moduleName);
        if ((initLowGatePinPair == null) && (initHighGatePinPair == null)) {
            resetByAddingGate(circuit, component, initLowOutputs, resetPort, isActiveLow);
            resetByAddingGate(circuit, component, initHighOutputs, resetPort, isActiveLow);
        } else {
            if ((initLowGatePinPair != null) && ((initLowOutputs.size() > initHighOutputs.size()) || (initHighGatePinPair == null))) {
                resetByReplacingGate(circuit, component, initLowOutputs, resetPort, isActiveLow, initLowGatePinPair);
                resetByAddingGate(circuit, component, initHighOutputs, resetPort, isActiveLow);
            } else {
                resetByReplacingGate(circuit, component, initHighOutputs, resetPort, isActiveLow, initHighGatePinPair);
                resetByAddingGate(circuit, component, initLowOutputs, resetPort, isActiveLow);
            }
        }
    }

    private static void resetUnmappedComponent(VisualCircuit circuit, VisualFunctionComponent component,
            VisualContact resetPort, boolean isActiveLow) {

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
        resetByReplacingGate(circuit, component, initLowOutputs, resetPort, isActiveLow, Pair.of("", "R"));
        resetByReplacingGate(circuit, component, initHighOutputs, resetPort, isActiveLow, Pair.of("", "S"));
    }

    private static void resetByAddingGate(VisualCircuit circuit, VisualFunctionComponent component,
            Collection<VisualFunctionContact> forcedInitOutputs, VisualContact resetPort, boolean isActiveLow) {

        for (VisualFunctionContact contact : forcedInitOutputs) {
            insertResetGate(circuit, resetPort, contact, isActiveLow);
        }
        GateUtils.propagateInitialState(circuit, component, forcedInitOutputs);
    }

    private static void resetByReplacingGate(VisualCircuit circuit, VisualFunctionComponent component,
            Collection<VisualFunctionContact> forcedInitOutputs, VisualContact resetPort,
            boolean isActiveLow, Pair<String, String> initGatePinPair) {

        if (!forcedInitOutputs.isEmpty()) {
            VisualFunctionContact initContact = getOrCreateResetPin(circuit, component, initGatePinPair.getSecond());
            CircuitUtils.connectIfPossible(circuit, resetPort, initContact);
            ConversionUtils.replicateDriverContact(circuit, initContact);
            for (VisualFunctionContact contact : forcedInitOutputs) {
                insertResetFunction(contact, initContact, isActiveLow);
            }
            component.getReferencedComponent().setModule(initGatePinPair.getFirst());
        }
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
        Contact initVar = resetContact.getReferencedComponent();
        boolean initToOne = contact.getInitToOne();
        contact.setBothFunctions(
                getFormulaWithReset(setFunction, initVar, activeLow, initToOne),
                getFormulaWithReset(resetFunction, initVar, activeLow, !initToOne));
    }

    private static void insertResetGate(VisualCircuit circuit, VisualContact resetPort,
            VisualFunctionContact contact, boolean activeLow) {

        SpaceUtils.makeSpaceAroundContact(circuit, contact, 3.0);
        VisualFunctionComponent resetGate = createResetGate(circuit, contact.getInitToOne(), activeLow);
        GateUtils.insertGateAfter(circuit, resetGate, contact, 2.0);
        CircuitUtils.connectToHangingInputPins(circuit, resetPort, resetGate, true);
        GateUtils.propagateInitialState(circuit, resetGate);
    }

    private static VisualFunctionComponent createResetGate(VisualCircuit circuit, boolean initToOne, boolean activeLow) {
        if (activeLow) {
            return initToOne ? GateUtils.createNand2bGate(circuit) : GateUtils.createAnd2Gate(circuit);
        } else {
            return initToOne ? GateUtils.createOr2Gate(circuit) : GateUtils.createNor2bGate(circuit);
        }
    }

    private static void setInitialisationProtocol(VisualCircuit circuit, VisualFunctionContact resetPort, boolean activeLow) {
        resetPort.setInitToOne(!activeLow);
        resetPort.setForcedInit(true);
        resetPort.setBothFunctions(activeLow ? One.getInstance() : Zero.getInstance(),
                activeLow ? Zero.getInstance() : One.getInstance());

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
        String forcedPortText = '\n' + TextUtils.getBulletpoint(forcedPortRefs.isEmpty() ? "No forced input ports"
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
        String uninitialisedPinText = '\n' + TextUtils.getBulletpoint(TextUtils.wrapMessageWithItems(
                "Uninitialised signal", uninitialisedPinRefs));

        DialogUtils.showError("Circuit is not fully initialised via forced input ports."
                + forcedPortText + uninitialisedPinText, VERIFICATION_RESULT_TITLE);

        return false;
    }

    public static boolean isResetInputPortName(String portName) {
        return MatchingUtils.isMatchingExact(portName, CircuitSettings.getResetActiveLowPort())
                || MatchingUtils.isMatchingExact(portName, CircuitSettings.getResetActiveHighPort());
    }

}
