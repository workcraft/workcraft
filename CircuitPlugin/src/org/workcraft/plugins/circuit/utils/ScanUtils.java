package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;

import java.util.*;

public class ScanUtils {

    public static Set<VisualFunctionComponent> insertTestableBuffers(VisualCircuit circuit) {
        Set<VisualFunctionComponent> components = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            for (VisualContact contact : component.getVisualOutputs()) {
                if (contact.getReferencedContact().getPathBreaker()) {
                    contact.getReferencedContact().setPathBreaker(false);
                    VisualFunctionComponent buffer = insertOrReuseBuffer(circuit, contact);
                    components.add(buffer);
                }
            }
        }
        return components;
    }

    private static VisualFunctionComponent insertOrReuseBuffer(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = null;
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) parent;
            if (isBuffer(component.getReferencedComponent())) {
                result = component;
            }
        }
        if ((result == null) && contact.isOutput()) {
            Collection<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, contact, false);
            if (drivenContacts.size() == 1) {
                VisualContact drivenContact = drivenContacts.iterator().next();
                result = insertOrReuseBuffer(circuit, drivenContact);
            }
        }
        if ((result == null) && contact.isOutput()) {
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact);
            GateUtils.propagateInitialState(circuit, result);
            result.getGateOutput().getReferencedContact().setPathBreaker(true);
        }
        if (result != null)  {
            Gate2 tbuf = CircuitSettings.parseTbufData();
            result.getReferencedComponent().setModule(tbuf.name);
            circuit.setMathName(result.getFirstVisualInput(), tbuf.in);
            circuit.setMathName(result.getFirstVisualOutput(), tbuf.out);
            result.getGateOutput().getReferencedContact().setPathBreaker(true);
        }
        return result;
    }

    public static void insertScan(VisualCircuit circuit) {
        Set<VisualFunctionComponent> components = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            if (hasPathBreakerOutput(component.getReferencedComponent())) {
                components.add(component);
            }
        }
        if (!components.isEmpty()) {
            List<VisualFunctionContact> ports = new ArrayList<>();
            for (String name : CircuitSettings.parseScanPorts()) {
                ports.add(CircuitUtils.getOrCreatePort(circuit, name, Contact.IOType.INPUT));
            }

            for (VisualFunctionComponent component : components) {
                if (hasPathBreakerOutput(component.getReferencedComponent())) {
                    insertScan(circuit, component, ports);
                }
            }

            for (VisualFunctionContact port : ports) {
                SpaceUtils.positionPort(circuit, port);
                port.setSetFunction(Zero.instance());
            }
        }
    }

    private static void insertScan(VisualCircuit circuit, VisualFunctionComponent component,
            List<VisualFunctionContact> ports) {

        component.setRenderType(ComponentRenderingResult.RenderType.BOX);
        Iterator<VisualFunctionContact> iterator = ports.iterator();
        for (String name : CircuitSettings.parseScanPins()) {
            String ref = NamespaceHelper.getReference(circuit.getMathReference(component), name);
            VisualFunctionContact contact = circuit.getVisualComponentByMathReference(ref, VisualFunctionContact.class);
            if (contact == null) {
                contact = circuit.getOrCreateContact(component, name, Contact.IOType.INPUT);
                component.setPositionByDirection(contact, VisualContact.Direction.WEST, false);
            }
            if (iterator.hasNext()) {
                VisualFunctionContact port = iterator.next();
                if (circuit.getConnections(contact).isEmpty()) {
                    try {
                        circuit.connect(port, contact);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static boolean hasPathBreakerOutput(CircuitComponent component) {
        for (Contact outputContact : component.getOutputs()) {
            if (outputContact.getPathBreaker()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBuffer(FunctionComponent component) {
        boolean result = false;
        Collection<FunctionContact> contacts = component.getFunctionContacts();
        FunctionContact inputContact = null;
        FunctionContact outputContact = null;
        if (contacts.size() > 1) {
            inputContact = (FunctionContact) component.getFirstInput();
            outputContact = (FunctionContact) component.getFirstOutput();
        }
        if ((inputContact != null) && (outputContact != null)) {
            BooleanFormula setFunction = outputContact.getSetFunction();
            if ((setFunction != null) && (outputContact.getResetFunction() == null)) {
                BooleanFormula zeroReplace = BooleanUtils.replaceClever(setFunction, inputContact, Zero.instance());
                BooleanFormula oneReplace = BooleanUtils.replaceClever(setFunction, inputContact, One.instance());
                result = (zeroReplace == Zero.instance()) && (oneReplace == One.instance());
            }
        }
        return result;
    }

    public static boolean check(Circuit circuit) {
        Set<FunctionComponent> cycleComponents = CycleUtils.getCycledComponents(circuit);
        if (cycleComponents.isEmpty()) {
            return true;
        }
        ArrayList<String> refs = ReferenceHelper.getReferenceList(circuit, cycleComponents);
        String msg = "All cycles must be broken before inserting testable buffers or scan.\n" +
                LogUtils.getTextWithRefs("Problematic component", refs);

        DialogUtils.showError(msg);
        return false;
    }
}
