package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.Zero;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.utils.DialogUtils;

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
    public static void insertScan(VisualCircuit circuit) throws InvalidConnectionException {
        List<VisualFunctionComponent> components = new ArrayList<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            if (hasPathBreakerOutput(component.getReferencedComponent())) {
                components.add(component);
            }
        }
        // Arrange SCAN components from right-to-left (and in the same column from bottom-to-top)
        Collections.sort(components, new Comparator<VisualFunctionComponent>() {
            @Override
            public int compare(VisualFunctionComponent o1, VisualFunctionComponent o2) {
                int result = Double.compare(o2.getRootSpaceX(), o1.getRootSpaceX());
                return result == 0 ? Double.compare(o2.getRootSpaceY(), o1.getRootSpaceY()) : result;
            }
        });

        if (!components.isEmpty()) {
            String scanckName = CircuitSettings.parseScanckPortPin().getFirst();
            VisualFunctionContact scanckPort = CircuitUtils.getOrCreatePort(
                    circuit, scanckName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

            String scanenName = CircuitSettings.parseScanenPortPin().getFirst();
            VisualFunctionContact scanenPort = CircuitUtils.getOrCreatePort(
                    circuit, scanenName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

            String scaninName = CircuitSettings.parseScaninPortPin().getFirst();
            VisualFunctionContact scaninPort = CircuitUtils.getOrCreatePort(
                    circuit, scaninName, Contact.IOType.INPUT, VisualContact.Direction.EAST);

            VisualFunctionContact scanoutPin = scaninPort;
            for (VisualFunctionComponent component : components) {
                if (hasPathBreakerOutput(component.getReferencedComponent())) {
                    scanoutPin = insertScan(circuit, component, scanckPort, scanenPort, scanoutPin);
                }
            }

            SpaceUtils.positionPort(circuit, scanckPort, false);
            scanckPort.setSetFunction(Zero.instance());

            SpaceUtils.positionPort(circuit, scanenPort, false);
            scanenPort.setSetFunction(Zero.instance());

            SpaceUtils.positionPort(circuit, scaninPort, true);
            scaninPort.setSetFunction(Zero.instance());

            // If the last pin of SCAN chain is connected to an output port, use that port as scanout.
            // (This is necessary because a fork to several output ports is not allowed.)

            String scanoutName = CircuitSettings.parseScanoutPortPin().getFirst();

            Contact existingPort = CircuitUtils.getDrivenOutputPort(circuit.getMathModel(), scanoutPin.getReferencedContact());
            if (existingPort == null) {
                VisualFunctionContact scanoutPort = CircuitUtils.getOrCreatePort(
                        circuit, scanoutName, Contact.IOType.OUTPUT, VisualContact.Direction.EAST);

                circuit.connect(scanoutPin, scanoutPort);
                SpaceUtils.positionPort(circuit, scanoutPort, true);
                scanoutPort.setSetFunction(Zero.instance());
            } else {
                String ref = circuit.getMathReference(existingPort);
                if (!scanoutName.equals(ref)) {
                    String msg = "Existing port '" + ref + "' is used as SCAN chain output instead of '" + scanoutName + "'."
                            + "\nThis is necessary because a fork on several output ports is not allowed.";

                    DialogUtils.showWarning(msg, "Insertion of SCAN");
                }
            }
        }
    }

    private static VisualFunctionContact insertScan(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact scanckPort, VisualFunctionContact scanenPort, VisualFunctionContact dataContact) {

        component.setRenderType(ComponentRenderingResult.RenderType.BOX);
        String moduleName = component.getReferencedComponent().getModule();
        String scanSuffix = CircuitSettings.getScanSuffix();
        if (!moduleName.isEmpty() && !moduleName.endsWith(scanSuffix)) {
            component.getReferencedComponent().setModule(moduleName + scanSuffix);
        }

        VisualFunctionContact scanckPin = CircuitUtils.getOrCreateContact(circuit, component,
                CircuitSettings.parseScanckPortPin().getSecond(),
                Contact.IOType.INPUT, VisualContact.Direction.WEST);

        VisualFunctionContact scanenPin = CircuitUtils.getOrCreateContact(circuit, component,
                CircuitSettings.parseScanenPortPin().getSecond(),
                Contact.IOType.INPUT, VisualContact.Direction.WEST);

        String scaninName = CircuitSettings.parseScaninPortPin().getSecond();
        String ref = NamespaceHelper.getReference(circuit.getMathReference(component), scaninName);
        boolean noScaninPin = circuit.getVisualComponentByMathReference(ref, VisualFunctionContact.class) == null;
        VisualFunctionContact scaninPin = CircuitUtils.getOrCreateContact(circuit, component,
                scaninName, Contact.IOType.INPUT, VisualContact.Direction.EAST);

        if (noScaninPin) {
            scaninPin.setY(scaninPin.getY() + 0.5);
        }

        VisualFunctionContact scanoutPin = null;
        if (component.getVisualOutputs().size() == 1) {
            scanoutPin = component.getFirstVisualOutput();
        } else {
            scanoutPin = CircuitUtils.getOrCreateContact(circuit, component,
                    CircuitSettings.parseScanoutPortPin().getSecond(),
                    Contact.IOType.OUTPUT, VisualContact.Direction.EAST);
        }

        try {
            if (circuit.getConnections(scanckPin).isEmpty()) {
                circuit.connect(scanckPort, scanckPin);
            }
            if (circuit.getConnections(scanenPin).isEmpty()) {
                circuit.connect(scanenPort, scanenPin);
            }
            if (circuit.getConnections(scaninPin).isEmpty()) {
                circuit.connect(dataContact, scaninPin);
            }
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
        return scanoutPin;
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
