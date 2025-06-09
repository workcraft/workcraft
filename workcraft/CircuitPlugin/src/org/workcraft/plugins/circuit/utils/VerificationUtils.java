package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class VerificationUtils {

    private static final String TITLE = "Verification";

    private VerificationUtils() {
    }

    public static Stg getEnvironmentStg(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        File envFile = circuit.getEnvironmentFile();
        return StgUtils.loadOrImportStg(envFile);
    }

    public static boolean checkCircuitHasComponents(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (circuit.getFunctionComponents().isEmpty()) {
            DialogUtils.showError("The circuit must have components.");
            return false;
        }
        return true;
    }

    public static boolean checkInterfaceInitialState(WorkspaceEntry we) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        File envFile = circuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadOrImportStg(envFile);
        // Check initial state conformance of interface signals between the circuit and its environment STG (if present)
        if (envStg != null) {
            Set<String> conflictSignals = getConflictingInterfaceSignals(circuit, envStg);
            if (!conflictSignals.isEmpty()) {
                String msg = "The circuit and its environment have different initial state of interface signal";
                DialogUtils.showError(TextUtils.wrapMessageWithItems(msg, conflictSignals));
                return false;
            }
        }
        return true;
    }

    private static Set<String> getConflictingInterfaceSignals(Circuit circuit, Stg envStg) {
        Set<String> inconsistentSignals = new HashSet<>();
        Map<String, Boolean> envSignalStates = StgUtils.getInitialState(envStg, 2000);
        for (Contact port : circuit.getPorts()) {
            String portRef = circuit.getNodeReference(port);
            Boolean envSignalState = envSignalStates.get(portRef);
            Contact driver = CircuitUtils.findDriver(circuit, port, false);
            if ((envSignalState != null) && (driver != null) && (driver.getInitToOne() != envSignalState)) {
                inconsistentSignals.add(portRef);
            }
        }
        return inconsistentSignals;
    }

    public static boolean checkInterfaceConstrains(WorkspaceEntry we) {
        return checkInterfaceConstrains(we, false);
    }

    public static boolean checkInterfaceConstrains(WorkspaceEntry we, boolean skipEnvironmentCheck) {
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        File envFile = circuit.getEnvironmentFile();
        Stg envStg = StgUtils.loadOrImportStg(envFile);
        String longMessage = "";
        String shortMessage = "";
        if (!skipEnvironmentCheck && (envStg == null)) {
            String itemText = (envFile == null)
                    ? ('\n' + PropertyHelper.BULLET_PREFIX + "Environment STG is missing.")
                    : ('\n' + PropertyHelper.BULLET_PREFIX + "Environment STG cannot be read from the file:\n"
                    + envFile.getAbsolutePath());

            longMessage += itemText;
            shortMessage += itemText;
        }
        // Restore signal types in the environment STG
        if (envStg != null) {
            Collection<String> circuitInputSignals = ReferenceHelper.getReferenceList(circuit, circuit.getInputPorts());
            Collection<String> circuitOutputSignals = ReferenceHelper.getReferenceList(circuit, circuit.getOutputPorts());
            StgUtils.restoreInterfaceSignals(envStg, circuitInputSignals, circuitOutputSignals);
        }
        // Check the circuit for hanging contacts
        Set<String> hangingSignals = getHangingSignals(circuit);
        if (!hangingSignals.isEmpty()) {
            String itemText = '\n' + PropertyHelper.BULLET_PREFIX + TextUtils.wrapMessageWithItems(
                    "Hanging contact", SortUtils.getSortedNatural(hangingSignals));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }
        // Check the circuit for unconstrained inputs and unused outputs
        Set<String> unconstrainedInputSignals = getUnconstrainedInputSignals(envStg, circuit);
        if (!unconstrainedInputSignals.isEmpty()) {
            String itemText = '\n' + PropertyHelper.BULLET_PREFIX + TextUtils.wrapMessageWithItems(
                    "Unconstrained input signal", SortUtils.getSortedNatural(unconstrainedInputSignals));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }
        // Check the circuit for unconstrained inputs and unused outputs
        Set<String> unusedOutputSignals = getUnusedOutputSignals(envStg, circuit).stream()
                .filter(signal -> !ScanUtils.isScanOutputPortName(signal))
                .collect(Collectors.toSet());

        if (!unusedOutputSignals.isEmpty()) {
            String itemText = '\n' + PropertyHelper.BULLET_PREFIX + TextUtils.wrapMessageWithItems(
                    "Unused output signal", SortUtils.getSortedNatural(unusedOutputSignals));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }
        // Check the circuit for excited components
        Set<String> excitedComponentRefs = getExcitedComponentRefs(circuit);
        if (!excitedComponentRefs.isEmpty()) {
            String itemText = '\n' + PropertyHelper.BULLET_PREFIX + TextUtils.wrapMessageWithItems(
                    "Excited component", SortUtils.getSortedNatural(excitedComponentRefs));

            longMessage += itemText;
            shortMessage += TextUtils.getHeadAndTail(itemText, 5, 0);
        }
        // Produce warning
        if (!longMessage.isEmpty()) {
            String title = circuit.getTitle();
            String intro = "The circuit " + (title.isEmpty() ? "" : ("'" + title + "' ")) + "has the following issues:";
            LogUtils.logWarning(intro + longMessage);
            return DialogUtils.showConfirm(intro + shortMessage, "\n\nProceed anyway?",
                    TITLE, true, JOptionPane.WARNING_MESSAGE, false);
        }
        return true;
    }

    public static Set<String> getHangingSignals(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver()) {
                if (CircuitUtils.findDriven(circuit, contact, false).isEmpty()) {
                    String signal = circuit.getNodeReference(contact);
                    result.add(signal);
                }
            } else {
                if (CircuitUtils.findDriver(circuit, contact, false) == null) {
                    String signal = circuit.getNodeReference(contact);
                    result.add(signal);
                }
            }
        }
        return result;
    }

    public static Set<String> getHangingDriverSignals(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            String signal = circuit.getNodeReference(contact);
            if (contact.isDriver()) {
                if (CircuitUtils.findDriven(circuit, contact, false).isEmpty()) {
                    result.add(signal);
                }
            }
        }
        return result;
    }

    private static Set<String> getUnconstrainedInputSignals(Stg envStg, Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (Contact contact : circuit.getInputPorts()) {
            if (!(contact instanceof FunctionContact inputPort)) continue;
            BooleanFormula setFunction = inputPort.getSetFunction();
            if (setFunction == null) {
                String inputSignal = circuit.getNodeReference(inputPort);
                result.add(inputSignal);
            }
        }
        if (envStg != null) {
            result.removeAll(envStg.getSignalReferences(Signal.Type.INPUT));
        }
        return result;
    }

    private static Set<String> getUnusedOutputSignals(Stg envStg, Circuit circuit) {
        Set<String> result = new HashSet<>();
        Set<BooleanVariable> literals = GateUtils.getUsedPortVariables(circuit);
        for (Contact contact : circuit.getOutputPorts()) {
            if (literals.contains(contact)) continue;
            String outputSignal = circuit.getNodeReference(contact);
            result.add(outputSignal);
        }
        if (envStg != null) {
            result.removeAll(envStg.getSignalReferences(Signal.Type.OUTPUT));
        }
        return result;
    }

    public static Set<String> getExcitedComponentRefs(Circuit circuit) {
        Set<String> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay() && GateUtils.isExcitedComponent(circuit, component)) {
                result.add(circuit.getComponentReference(component));
            }
        }
        return result;
    }

    public static boolean checkBlackboxComponents(WorkspaceEntry we) {
        return checkBlackboxComponents(WorkspaceUtils.getAs(we, Circuit.class));
    }

    public static boolean checkBlackboxComponents(Circuit circuit) {
        Set<FunctionComponent> blackboxComponents = circuit.getFunctionComponents().stream()
                .filter(FunctionComponent::isBlackbox)
                .collect(Collectors.toSet());

        if (!blackboxComponents.isEmpty()) {
            Collection<String> refs = ReferenceHelper.getReferenceList(circuit, blackboxComponents);
            String msg = TextUtils.wrapMessageWithItems("Circuit has blackbox component", refs);
            return DialogUtils.showConfirmWarning(msg, "\n\nProceed anyway?", TITLE, true);
        }
        return true;
    }

}
