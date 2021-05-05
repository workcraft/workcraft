package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.Zero;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.UnaryGateInterface;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.types.Pair;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;
import java.util.stream.Collectors;

public class ScanUtils {

    public static Set<VisualFunctionComponent> insertTestableGates(VisualCircuit circuit) {
        Set<VisualFunctionComponent> result = new HashSet<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            for (VisualContact contact : component.getVisualOutputs()) {
                if (contact.getReferencedComponent().getPathBreaker()) {
                    contact.getReferencedComponent().setPathBreaker(false);
                    VisualFunctionComponent testableGate = insertTestableGate(circuit, contact);
                    result.add(testableGate);
                }
            }
        }
        return result;
    }

    private static VisualFunctionComponent insertTestableGate(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = getAdjacentBufferOrInverter(circuit, contact);
        if (result == null) {
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact);
            GateUtils.propagateInitialState(circuit, result);
            result.getGateOutput().getReferencedComponent().setPathBreaker(true);
        }
        UnaryGateInterface testableGateInterface = result.isInverter() ? CircuitSettings.parseTinvData() : CircuitSettings.parseTbufData();
        result.getReferencedComponent().setModule(testableGateInterface.name);

        VisualFunctionContact inputContact = result.getFirstVisualInput();
        VisualFunctionContact outputContact = result.getFirstVisualOutput();
        // Temporary rename gate output, so there is no name clash on renaming gate input
        circuit.setMathName(outputContact, Identifier.makeInternal(testableGateInterface.output));
        circuit.setMathName(inputContact, testableGateInterface.input);
        circuit.setMathName(outputContact, testableGateInterface.output);
        result.getGateOutput().getReferencedComponent().setPathBreaker(true);
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

    public static boolean insertScan(WorkspaceEntry we) {
        boolean result = false;
        if (WorkspaceUtils.isApplicable(we, VisualCircuit.class)) {
            VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            we.captureMemento();
            try {
                result = ScanUtils.insertScan(circuit);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
        if (result) {
            we.saveMemento();
        } else {
            we.uncaptureMemento();
        }
        return result;
    }

    public static boolean insertScan(VisualCircuit circuit) throws InvalidConnectionException {
        String ckName = CircuitSettings.parseScanckPortPin().getFirst();
        String enName = CircuitSettings.parseScanenPortPin().getFirst();
        String tmName = CircuitSettings.parseScantmPortPin().getFirst();
        String inName = CircuitSettings.parseScaninPortPin().getFirst();

        Collection<String> scanContactNames = Arrays.asList(ckName, enName, tmName, inName);

        List<VisualFunctionComponent> scanComponents = circuit.getVisualFunctionComponents().stream()
                .filter(component -> hasPathBreakerOutput(component)
                        || CircuitUtils.hasContacts(circuit, component, scanContactNames))
                .collect(Collectors.toList());

        boolean result = !scanComponents.isEmpty();
        if (result) {
            VisualFunctionContact ckPort = getOrCreateAlwaysLowInputPort(circuit, ckName, VisualContact.Direction.WEST);
            VisualFunctionContact enPort = getOrCreateAlwaysLowInputPort(circuit, enName, VisualContact.Direction.WEST);
            VisualFunctionContact tmPort = getOrCreateAlwaysLowInputPort(circuit, tmName, VisualContact.Direction.WEST);
            VisualFunctionContact inPort = getOrCreateAlwaysLowInputPort(circuit, inName, VisualContact.Direction.EAST);

            for (VisualFunctionComponent component : scanComponents) {
                if (hasPathBreakerOutput(component)) {
                    convertPathbreakerToScan(circuit, component);
                }
                connectComponentToScan(circuit, component, ckPort, enPort, tmPort);
            }

            if (CircuitSettings.getStitchScan()) {
                List<VisualFunctionComponent> orderedScanComponents = scanComponents.stream()
                        .sorted((o1, o2) -> {
                            int xComparison = Double.compare(o2.getRootSpaceX(), o1.getRootSpaceX());
                            return xComparison == 0 ? Double.compare(o2.getRootSpaceY(), o1.getRootSpaceY()) : xComparison;
                        })
                        .collect(Collectors.toList());

                stitchScanComponents(circuit, orderedScanComponents, inPort);
            } else {
                // If no stitching, still create scanout port and position it on the right of the design
                createScanoutPort(circuit);
            }
            // Set position and function of scan ports
            SpaceUtils.positionPort(circuit, ckPort, false);
            SpaceUtils.positionPort(circuit, enPort, false);
            SpaceUtils.positionPort(circuit, tmPort, false);
            SpaceUtils.positionPort(circuit, inPort, true);
        }
        return result;
    }

    private static VisualFunctionContact getOrCreateAlwaysLowInputPort(VisualCircuit circuit, String portName,
            VisualContact.Direction direction) {

        VisualFunctionContact port = CircuitUtils.getOrCreatePort(circuit, portName, Contact.IOType.INPUT, direction);
        if (port != null) {
            port.setSetFunction(Zero.getInstance());
        }
        return port;
    }

    private static void convertPathbreakerToScan(VisualCircuit circuit, VisualFunctionComponent component) {
        component.setRenderType(ComponentRenderingResult.RenderType.BOX);
        String moduleName = component.getReferencedComponent().getModule();
        String scanSuffix = CircuitSettings.getScanSuffix();
        if (!moduleName.isEmpty() && !moduleName.endsWith(scanSuffix)) {
            component.getReferencedComponent().setModule(moduleName + scanSuffix);
        }

        String ckName = CircuitSettings.parseScanckPortPin().getSecond();
        CircuitUtils.getOrCreateContact(circuit, component, ckName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

        String enName = CircuitSettings.parseScanenPortPin().getSecond();
        CircuitUtils.getOrCreateContact(circuit, component, enName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

        String tmName = CircuitSettings.parseScantmPortPin().getSecond();
        CircuitUtils.getOrCreateContact(circuit, component, tmName, Contact.IOType.INPUT, VisualContact.Direction.WEST);

        String inName = CircuitSettings.parseScaninPortPin().getSecond();
        VisualFunctionContact inPin = CircuitUtils.getFunctionContact(circuit, component, inName);
        if (inPin == null) {
            inPin = CircuitUtils.getOrCreateContact(circuit, component,
                    inName, Contact.IOType.INPUT, VisualContact.Direction.EAST);

            inPin.setY(inPin.getY() + 1.5);
        }

        if (component.getVisualOutputs().size() != 1) {
            String outName = CircuitSettings.parseScanoutPortPin().getSecond();
            CircuitUtils.getOrCreateContact(circuit, component, outName,
                    Contact.IOType.OUTPUT, VisualContact.Direction.EAST);
        }
    }

    private static void connectComponentToScan(VisualCircuit circuit, VisualFunctionComponent component,
            VisualContact ckPort, VisualContact enPort, VisualContact tmPort) {

        VisualFunctionContact ckPin = getPinFromPair(circuit, component, CircuitSettings.parseScanckPortPin());
        connectIfPossible(circuit, ckPort, ckPin);

        VisualFunctionContact enPin = getPinFromPair(circuit, component, CircuitSettings.parseScanenPortPin());
        connectIfPossible(circuit, enPort, enPin);

        VisualFunctionContact tmPin = getPinFromPair(circuit, component, CircuitSettings.parseScantmPortPin());
        connectIfPossible(circuit, tmPort, tmPin);
    }

    private static VisualFunctionContact getPinFromPair(VisualCircuit circuit, VisualFunctionComponent component,
            Pair<String, String> portPinPair) {

        for (String name : Arrays.asList(portPinPair.getFirst(), portPinPair.getSecond())) {
            if (circuit.hasPin(component, name)) {
                return circuit.getPin(component, name);
            }
        }
        return null;
    }

    private static void connectIfPossible(VisualCircuit circuit, VisualContact fromContact, VisualContact toContact) {
        if (circuit.getConnections(toContact).isEmpty()) {
            try {
                circuit.connect(fromContact, toContact);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void stitchScanComponents(VisualCircuit circuit, List<VisualFunctionComponent> scanComponents,
            VisualContact inPort) {

        // Disconnect components from old scan chain
        for (VisualFunctionComponent component : scanComponents) {
            VisualFunctionContact inPin = getPinFromPair(circuit, component, CircuitSettings.parseScaninPortPin());
            CircuitUtils.disconnectContact(circuit, inPin);

            VisualFunctionContact outPin = getPinFromPair(circuit, component, CircuitSettings.parseScanoutPortPin());
            CircuitUtils.disconnectContact(circuit, outPin);
        }

        // If scanin port is connected then prepend existing scan chain, otherwise create a new scan chain
        Set<VisualContact> oldScaninContacts = CircuitUtils.findDriven(circuit, inPort, false);

        CircuitUtils.disconnectContact(circuit, inPort);

        VisualContact contact = inPort;
        for (VisualFunctionComponent component : scanComponents) {
            contact = stitchScanComponent(circuit, component, contact);
        }

        if (oldScaninContacts.isEmpty()) {
            connectScanoutPort(circuit, contact);
        } else {
            connectExistingScanChain(circuit, contact, oldScaninContacts);
        }
    }

    private static VisualContact stitchScanComponent(VisualCircuit circuit, VisualFunctionComponent component,
            VisualContact contact) {

        if (contact != null) {
            VisualFunctionContact inPin = getPinFromPair(circuit, component, CircuitSettings.parseScaninPortPin());
            connectIfPossible(circuit, contact, inPin);
        }
        return (component.getVisualOutputs().size() == 1) ? component.getFirstVisualOutput()
                : getPinFromPair(circuit, component, CircuitSettings.parseScanoutPortPin());
    }

    private static void connectScanoutPort(VisualCircuit circuit, VisualContact dataPin) {

        // If the last pin of scan chain is connected to an output port, use that port as scanout.
        // (This is necessary because a fork to several output ports is not allowed.)
        String outName = CircuitSettings.parseScanoutPortPin().getFirst();
        Contact existingPort = CircuitUtils.getDrivenOutputPort(circuit.getMathModel(), dataPin.getReferencedComponent());
        if (existingPort == null) {
            VisualFunctionContact outPort = CircuitUtils.getOrCreatePort(circuit, outName,
                    Contact.IOType.OUTPUT, VisualContact.Direction.EAST);

            connectIfPossible(circuit, dataPin, outPort);
            SpaceUtils.positionPort(circuit, outPort, true);
        } else {
            String portName = circuit.getMathReference(existingPort);
            if (!outName.equals(portName)) {
                String msg = "Existing port '" + portName + "' is used as SCAN chain output instead of '" + outName + "'."
                        + "\nThis is necessary because a fork on several output ports is not allowed.";

                DialogUtils.showWarning(msg, "Insertion of SCAN");
            }
        }
    }

    private static void connectExistingScanChain(VisualCircuit circuit, VisualContact dataContact,
            Set<VisualContact> drivenContacts) {

        for (VisualContact outContact : drivenContacts) {
            connectIfPossible(circuit, dataContact, outContact);
        }
    }

    private static void createScanoutPort(VisualCircuit circuit) {
        String outName = CircuitSettings.parseScanoutPortPin().getFirst();
        VisualFunctionContact outPort = CircuitUtils.getOrCreatePort(
                circuit, outName, Contact.IOType.OUTPUT, VisualContact.Direction.EAST);

        SpaceUtils.positionPort(circuit, outPort, true);
    }

    public static boolean hasPathBreakerOutput(VisualCircuitComponent component) {
        return hasPathBreakerOutput(component.getReferencedComponent());
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
