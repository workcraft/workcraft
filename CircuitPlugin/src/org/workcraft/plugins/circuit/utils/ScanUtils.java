package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.Zero;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;

import java.util.*;

public class ScanUtils {

    public static Set<VisualFunctionComponent> insertTestableGates(VisualCircuit circuit) {
        Set<VisualFunctionComponent> components = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            for (VisualContact contact : component.getVisualOutputs()) {
                if (contact.getReferencedContact().getPathBreaker()) {
                    contact.getReferencedContact().setPathBreaker(false);
                    VisualFunctionComponent testableGate = insertTestableGate(circuit, contact);
                    if (testableGate != null) {
                        components.add(testableGate);
                    }
                }
            }
        }
        return components;
    }

    private static VisualFunctionComponent insertTestableGate(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = getAdjacentBufferOrInverter(circuit, contact);
        if (result == null) {
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact);
            GateUtils.propagateInitialState(circuit, result);
            result.getGateOutput().getReferencedContact().setPathBreaker(true);
        }
        Gate2 testableGate = result.isInverter() ? CircuitSettings.parseTinvData() : CircuitSettings.parseTbufData();
        result.getReferencedComponent().setModule(testableGate.name);
        circuit.setMathName(result.getFirstVisualInput(), testableGate.in);
        circuit.setMathName(result.getFirstVisualOutput(), testableGate.out);
        result.getGateOutput().getReferencedContact().setPathBreaker(true);
        return result;
    }

    private static VisualFunctionComponent getAdjacentBufferOrInverter(VisualCircuit circuit, VisualContact contact) {
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) parent;
            if (component.isBuffer() || component.isInverter()) {
                return component;
            }
        }
        if (contact.isOutput()) {
            Collection<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, contact, false);
            if (drivenContacts.size() == 1) {
                VisualContact drivenContact = drivenContacts.iterator().next();
                return getAdjacentBufferOrInverter(circuit, drivenContact);
            }
        }
        return null;
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
        String moduleName = component.getReferencedComponent().getModule();
        if (!moduleName.isEmpty()) {
            component.getReferencedComponent().setModule(moduleName + CircuitSettings.getScanSuffix());
        }
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

}
