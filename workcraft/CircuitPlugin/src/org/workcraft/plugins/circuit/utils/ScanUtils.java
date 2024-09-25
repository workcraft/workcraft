package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.formula.*;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.genlib.GateInterface;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;
import org.workcraft.utils.LogUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ScanUtils {

    private static class ScanData {
        public final String scaninPortPrefix = CircuitSettings.getScaninPort();
        public final String scanenPortPrefix = CircuitSettings.getScanenPort();
        public final String scanoutPortPrefix = CircuitSettings.getScanoutPort();
        public final String scaninPinName = CircuitSettings.getScaninPin();
        public final String scanenPinName = CircuitSettings.getScanenPin();
        public final String scanoutPinName = CircuitSettings.getScanoutPin();
    }

    private ScanUtils() {
    }

    public static List<VisualFunctionComponent> insertTestableGates(VisualCircuit circuit) {
        List<VisualFunctionComponent> result = new ArrayList<>();
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            for (VisualContact contact : component.getVisualOutputs()) {
                if (contact.getReferencedComponent().getPathBreaker()) {
                    contact.getReferencedComponent().setPathBreaker(false);
                    result.add(insertTestableGate(circuit, contact));
                }
            }
        }
        for (VisualFunctionComponent testableGate : result) {
            boolean initToOne = testableGate.getFirstVisualOutput().getInitToOne();
            String testInstanceName = CircuitSettings.getTestInstanceName(initToOne);
            circuit.setMathName(testableGate, testInstanceName);
        }
        return result;
    }

    private static VisualFunctionComponent insertTestableGate(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = getAdjacentBufferOrInverter(circuit, contact);
        if (result == null) {
            SpaceUtils.makeSpaceAroundContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact, 2.0);
            GateUtils.propagateInitialState(circuit, result);
            result.getGateOutput().getReferencedComponent().setPathBreaker(true);
        }
        GateInterface testableGateInterface = result.isInverter() ? CircuitSettings.parseTinvData() : CircuitSettings.parseTbufData();
        result.getReferencedComponent().setModule(testableGateInterface.getName());

        VisualContact inputContact = result.getFirstVisualInput();
        VisualContact outputContact = result.getFirstVisualOutput();
        // Temporary rename gate output, so there is no name clash on renaming gate input
        String inputName = testableGateInterface.getInputs().get(0);
        String outputName = testableGateInterface.getOutput();
        circuit.setMathName(outputContact, Identifier.getTemporaryName());
        circuit.setMathName(inputContact, inputName);
        circuit.setMathName(outputContact, outputName);
        if (CircuitSettings.getUseTestPathBreaker()) {
            inputContact.getReferencedComponent().setPathBreaker(true);
        }
        outputContact.getReferencedComponent().setPathBreaker(true);
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
        if (!contact.isOutput()) {
            return null;
        }
        Collection<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, contact, false);
        if (drivenContacts.size() != 1) {
            return null;
        }
        VisualContact drivenContact = drivenContacts.iterator().next();
        if (drivenContact == contact) {
            return null;
        }
        return getAdjacentBufferOrInverter(circuit, drivenContact);
    }

    public static boolean insertScan(VisualCircuit circuit) {
        if (CircuitSettings.getUseIndividualScan()) {
            return insertScanIndividual(circuit);
        } else {
            return insertScanChain(circuit);
        }
    }

    private static boolean insertScanIndividual(VisualCircuit circuit) {
        List<VisualFunctionComponent> scanComponents = circuit.getVisualFunctionComponents().stream()
                .filter(component -> hasPathBreakerOutput(component)
                        || hasContactWithPrefix(component, CircuitSettings.getScaninPort())
                        || hasContactWithPrefix(component, CircuitSettings.getScanoutPort())
                        || hasContactWithPrefix(component, CircuitSettings.getScanenPort())
                        || hasContactWithPrefix(component, CircuitSettings.getScanckPort())
                        || hasContactWithPrefix(component, CircuitSettings.getScantmPort()))
                .collect(Collectors.toList());

        Set<VisualFunctionComponent> scaninInversionComponents = scanComponents.stream()
                .filter(ScanUtils::needsScaninInversion)
                .collect(Collectors.toSet());

        convertPathbreakerToScan(circuit, scanComponents);
        // Add individual scan ports and connect them to corresponding scan pins - scanin/SI, scanout/SO, scanen/SE
        connectIndividualScan(circuit, scanComponents, scaninInversionComponents);

        // Add always common scan port and connect them to corresponding scan pins - scanck/CK and scantm/TM
        connectCommonInputPort(circuit, scanComponents,
                CircuitSettings.getScanckPort(), CircuitSettings.getScanckPin(), false);

        connectCommonInputPort(circuit, scanComponents,
                CircuitSettings.getScantmPort(), CircuitSettings.getScantmPin(), false);

        return !scanComponents.isEmpty();
    }

    private static boolean hasContactWithPrefix(VisualCircuitComponent component, String prefix) {
        if ((prefix != null) && !prefix.isEmpty()) {
            return component.getVisualContacts().stream()
                    .anyMatch(contact -> contact.getName().startsWith(prefix));
        }
        return false;
    }

    private static boolean needsScaninInversion(VisualFunctionComponent component) {
        if (component.isGate()) {
            VisualFunctionContact outputContact = component.getFirstVisualOutput();
            return component.isInverter() != outputContact.getInitToOne();
        }
        return false;
    }

    private static void convertPathbreakerToScan(VisualCircuit circuit, List<VisualFunctionComponent> scanComponents) {
        for (VisualFunctionComponent component : scanComponents) {
            if (hasPathBreakerOutput(component)) {
                convertPathbreakerToScan(circuit, component);
            }
        }
    }

    private static void convertPathbreakerToScan(VisualCircuit circuit, VisualFunctionComponent component) {
        component.setRenderType(ComponentRenderingResult.RenderType.BOX);
        String moduleName = component.getReferencedComponent().getModule();
        String scanSuffix = CircuitSettings.getScanSuffix();
        if (!moduleName.isEmpty() && (!moduleName.endsWith(scanSuffix) || scanSuffix.isEmpty())) {
            component.getReferencedComponent().setModule(moduleName + scanSuffix);
        }
        boolean isInverter = component.isInverter();

        VisualContact scanenPin = getOrCreateContact(circuit, component, CircuitSettings.getScanenPin(), Contact.IOType.INPUT);
        getOrCreateContact(circuit, component, CircuitSettings.getScanckPin(), Contact.IOType.INPUT);
        getOrCreateContact(circuit, component, CircuitSettings.getScantmPin(), Contact.IOType.INPUT);

        VisualContact scaninPin = null;
        if (CircuitSettings.getUseIndividualScan()) {
            scaninPin = getOrCreateContact(circuit, component, CircuitSettings.getScaninPin(), Contact.IOType.INPUT);
        } else {
            scaninPin = getOrCreateContact(circuit, component, CircuitSettings.getScaninPin(), Contact.IOType.INPUT,
                    VisualContact.Direction.EAST, new Point2D.Double(0.0, 1.5));
        }

        if (component.getVisualOutputs().size() == 1) {
            VisualFunctionContact outPin = component.getFirstVisualOutput();
            BooleanFormula setFunction = outPin.getSetFunction();
            Contact scaninVar = scaninPin.getReferencedComponent();
            BooleanFormula initVar = isInverter ? new Not(scaninVar) : scaninVar;
            Contact scanenVar = scanenPin.getReferencedComponent();
            BooleanFormula muxFunction = FormulaUtils.createMux(setFunction, initVar, scanenVar);
            outPin.setSetFunction(muxFunction);
        } else {
            getOrCreateContact(circuit, component, CircuitSettings.getScanoutPin(), Contact.IOType.OUTPUT);
        }
    }

    private static boolean insertScanChain(VisualCircuit circuit) {
        List<VisualFunctionComponent> scanComponents = circuit.getVisualFunctionComponents().stream()
                .filter(component -> hasPathBreakerOutput(component)
                        || (hasContactWithName(component, CircuitSettings.getScaninPort())
                        && hasContactWithName(component, CircuitSettings.getScanoutPort())))
                .collect(Collectors.toList());

        convertPathbreakerToScan(circuit, scanComponents);

        // Add always common scan port and connect them to corresponding scan pins - scanen/SE, scanck/CK, scantm/TM
        connectCommonInputPort(circuit, scanComponents,
                CircuitSettings.getScanenPort(), CircuitSettings.getScanenPin(), CircuitSettings.getUseScanInitialisation());

        connectCommonInputPort(circuit, scanComponents,
                CircuitSettings.getScanckPort(), CircuitSettings.getScanckPin(), false);

        connectCommonInputPort(circuit, scanComponents,
                CircuitSettings.getScantmPort(), CircuitSettings.getScantmPin(), false);

        // Stitch scan components in order of their position - left-to-right and top-to-bottom
        stitchScanComponents(circuit, SpaceUtils.orderComponentsByPosition(scanComponents));

        return !scanComponents.isEmpty();
    }

    private static boolean hasContactWithName(VisualCircuitComponent component, String name) {
        if ((name != null) && !name.isEmpty()) {
            return component.getVisualContacts().stream()
                    .anyMatch(contact -> contact.getName().equals(name));
        }
        return false;
    }

    private static void connectCommonInputPort(VisualCircuit circuit, List<VisualFunctionComponent> components,
            String portName, String pinName, boolean initToOne) {

        VisualContact port = getOrCreateNeverRiseForcedInitInputPort(circuit, portName,
                VisualContact.Direction.WEST, initToOne);

        if (port != null) {
            for (VisualFunctionComponent component : components) {
                VisualContact pin = getContactWithPathbreakerPinNameOrWithPortName(circuit, portName, component, pinName);
                CircuitUtils.connectIfPossible(circuit, port, pin);
            }
            SpaceUtils.positionPort(circuit, port, false);
            CircuitUtils.detachJoint(circuit, port, 0.5);
        }
    }

    private static VisualContact getOrCreateNeverRiseForcedInitInputPort(VisualCircuit circuit, String portName,
            VisualContact.Direction direction, boolean initToOne) {

        VisualFunctionContact result = null;
        if (portName != null) {
            result = CircuitUtils.getOrCreatePort(circuit, portName, Contact.IOType.INPUT, direction);
        }
        if (result != null) {
            result.setSetFunction(Zero.getInstance());
            result.setResetFunction(One.getInstance());
            result.setInitToOne(initToOne);
            result.setForcedInit(true);
        }
        return result;
    }

    private static VisualContact getContactWithPathbreakerPinNameOrWithPortName(VisualCircuit circuit, String portName,
            VisualFunctionComponent component, String pinName) {

        if (hasPathBreakerOutput(component) && (pinName != null)) {
            return circuit.getPin(component, pinName);
        }
        if (portName != null) {
            return circuit.getPin(component, portName);
        }
        return null;
    }

    private static void connectIndividualScan(VisualCircuit circuit, List<VisualFunctionComponent> components,
            Set<VisualFunctionComponent> scaninInversionComponents) {

        ScanData scanData = new ScanData();
        if ((scanData.scaninPortPrefix == null) || (scanData.scaninPinName == null)) {
            LogUtils.logError("Both scan input port and pin must be defined in global preferences");
            return;
        }
        if ((scanData.scanenPortPrefix == null) || (scanData.scanenPinName == null)) {
            LogUtils.logError("Both scan enable port and pin must be defined in global preferences");
            return;
        }
        if ((scanData.scanoutPortPrefix == null) && (scanData.scanoutPinName == null)) {
            LogUtils.logError("Both scan output port and pin must be defined in global preferences");
            return;
        }

        int index = 0;
        for (VisualFunctionComponent component : components) {
            // Connect to pathbreaker components (hierarchy leafs)
            boolean needScaninInversion = scaninInversionComponents.contains(component);
            index += connectIndividualScanPathbreaker(circuit, component, scanData, index, needScaninInversion);
            // Route scan nets through hierarchy
            index += connectIndividualScanHierarchy(circuit, component, scanData, index);
        }
    }

    private static int connectIndividualScanPathbreaker(VisualCircuit circuit, VisualFunctionComponent component,
            ScanData scanData, int index, boolean needScaninInversion) {

        if (!hasPathBreakerOutput(component)) {
            return 0;
        }

        VisualContact scaninPin = getScanPinOrLogError(circuit, component, scanData.scaninPinName);
        VisualContact scanenPin = getScanPinOrLogError(circuit, component, scanData.scanenPinName);
        VisualContact scanoutPin = getScanPinOrLogError(circuit, component, scanData.scanoutPinName);
        if ((scaninPin == null) || (scanenPin == null) || (scanoutPin == null)) {
            return 0;
        }

        VisualConnection scaninConnection = connectFromIndividualInputPort(
                circuit, scanData.scaninPortPrefix, index, scaninPin, false);

        if ((scaninConnection != null) && needScaninInversion) {
            VisualFunctionComponent inverterGate = GateUtils.createInverterGate(circuit);
            String invInstancePrefix = CircuitSettings.getInitialisationInverterInstancePrefix();
            circuit.setMathName(inverterGate, invInstancePrefix + index);
            GateUtils.insertGateWithin(circuit, inverterGate, scaninConnection);
            GateUtils.propagateInitialState(circuit, inverterGate);
        }

        boolean useScanInitialisation = CircuitSettings.getUseScanInitialisation();
        connectFromIndividualInputPort(circuit, scanData.scanenPortPrefix, index, scanenPin, useScanInitialisation);

        if (!scanoutPin.isDriver()) {
            scanoutPin = CircuitUtils.findDriver(circuit, scanoutPin, true);
        }
        if (needsBuffering(circuit, scanoutPin)) {
            scanoutPin = addBuffering(circuit, scanoutPin);
        }
        connectToIndividualOutputPort(circuit, scanoutPin, scanData.scanoutPortPrefix, index);
        return 1;
    }

    private static VisualContact getScanPinOrLogError(VisualCircuit circuit, VisualFunctionComponent component,
            String pinName) {

        VisualFunctionContact pin = circuit.getPin(component, pinName);
        if (pin == null) {
            String componentRef = circuit.getMathModel().getComponentReference(component.getReferencedComponent());
            LogUtils.logError("Cannot find pin '" + pinName + "' in scan component '" + componentRef + "'");
        }
        return pin;
    }

    private static int connectIndividualScanHierarchy(VisualCircuit circuit, VisualFunctionComponent component,
            ScanData scanData, int index) {

        List<VisualContact> scaninPins = MatchingUtils.getSortedMatchingInputPins(component, scanData.scaninPortPrefix);
        List<VisualContact> scanenPins = MatchingUtils.getSortedMatchingInputPins(component, scanData.scanenPortPrefix);
        List<VisualContact> scanoutPins = MatchingUtils.getSortedMatchingOutputPins(component, scanData.scanoutPortPrefix);

        int count = scaninPins.size();
        if ((scanenPins.size() != count) || (scanoutPins.size() != count)) {
            return 0;
        }

        boolean useScanInitialisation = CircuitSettings.getUseScanInitialisation();
        for (int i = 0; i < count; i++) {
            VisualContact scaninPin = scaninPins.get(i);
            connectFromIndividualInputPort(circuit, scanData.scaninPortPrefix, index + i, scaninPin, false);

            VisualContact scanenPin = scanenPins.get(i);
            connectFromIndividualInputPort(circuit, scanData.scanenPortPrefix, index + i, scanenPin, useScanInitialisation);

            VisualContact scanoutPin = scanoutPins.get(i);
            connectToIndividualOutputPort(circuit, scanoutPin, scanData.scanoutPortPrefix, index + i);
        }
        return count;
    }

    private static VisualConnection connectFromIndividualInputPort(VisualCircuit circuit, String portPrefix,
            int index, VisualContact pin, boolean initToOne) {

        String portName = MatchingUtils.getSignalWithBusSuffix(portPrefix, index);
        VisualContact port = getOrCreateNeverRiseForcedInitInputPort(circuit, portName,
                VisualContact.Direction.WEST, initToOne);

        CircuitUtils.disconnectContact(circuit, pin);
        VisualConnection connection = CircuitUtils.connectIfPossible(circuit, port, pin);
        SpaceUtils.positionPort(circuit, port, false);
        return connection;
    }

    private static VisualConnection connectToIndividualOutputPort(VisualCircuit circuit, VisualContact pin,
            String portPrefix, int index) {

        String portName = MatchingUtils.getSignalWithBusSuffix(portPrefix, index);
        VisualContact port = CircuitUtils.getOrCreatePort(circuit, portName,
                Contact.IOType.OUTPUT, VisualContact.Direction.EAST);

        CircuitUtils.disconnectContact(circuit, port);
        VisualConnection connection = CircuitUtils.connectIfPossible(circuit, pin, port);
        port.setInitToOne(pin.getInitToOne());
        SpaceUtils.positionPort(circuit, port, true);
        return connection;
    }

    private static VisualContact getOrCreateContact(VisualCircuit circuit, VisualFunctionComponent component,
            String contactName, Contact.IOType ioType) {

        return getOrCreateContact(circuit, component, contactName, ioType,
                ioType == Contact.IOType.INPUT ? VisualContact.Direction.WEST : VisualContact.Direction.EAST,
                new Point2D.Double(0.0, 0.0));
    }

    private static VisualContact getOrCreateContact(VisualCircuit circuit, VisualFunctionComponent component,
            String contactName, Contact.IOType ioType, VisualContact.Direction direction, Point2D offset) {

        VisualContact result = null;
        if ((contactName != null) && !contactName.isEmpty()) {
            result = CircuitUtils.getFunctionContact(circuit, component, contactName);
            if (result == null) {
                result = circuit.getOrCreateContact(component, contactName, ioType);
                component.setPositionByDirection(result, direction, false);
                result.setPosition(new Point2D.Double(result.getX() + offset.getX(), result.getY() + offset.getY()));
            }
        }
        return result;
    }

    private static void stitchScanComponents(VisualCircuit circuit, List<VisualFunctionComponent> components) {
        VisualContact scaninPort = getOrCreateNeverRiseForcedInitInputPort(circuit, CircuitSettings.getScaninPort(),
                VisualContact.Direction.EAST, false);

        // Disconnect components from old scan chain
        for (VisualFunctionComponent component : components) {
            VisualContact scaninPin = getContactWithPathbreakerPinNameOrWithPortName(circuit, CircuitSettings.getScaninPort(),
                    component, CircuitSettings.getScaninPin());

            CircuitUtils.disconnectContact(circuit, scaninPin);

            VisualContact scanoutPin = getContactWithPathbreakerPinNameOrWithPortName(circuit, CircuitSettings.getScanoutPort(),
                    component, CircuitSettings.getScanoutPin());

            if ((scanoutPin != null) && scanoutPin.isDriver()) {
                CircuitUtils.disconnectContact(circuit, scanoutPin);
            }
        }

        // If scanin port is connected then prepend existing scan chain, otherwise create a new scan chain
        Set<VisualContact> oldScaninContacts = CircuitUtils.findDriven(circuit, scaninPort, false);

        CircuitUtils.disconnectContact(circuit, scaninPort);

        VisualContact contact = scaninPort;
        for (VisualFunctionComponent component : components) {
            contact = stitchScanComponent(circuit, component, contact);
        }

        if (oldScaninContacts.isEmpty()) {
            connectScanoutPort(circuit, contact);
        } else {
            connectExistingScanChain(circuit, contact, oldScaninContacts);
        }
        SpaceUtils.positionPort(circuit, scaninPort, true);
    }

    private static VisualContact stitchScanComponent(VisualCircuit circuit, VisualFunctionComponent component,
            VisualContact contact) {

        if (contact != null) {
            VisualContact scaninPin = getContactWithPathbreakerPinNameOrWithPortName(circuit, CircuitSettings.getScaninPort(),
                    component, CircuitSettings.getScaninPin());

            CircuitUtils.connectIfPossible(circuit, contact, scaninPin);
        }
        return (component.getVisualOutputs().size() == 1) ? component.getFirstVisualOutput()
                : getContactWithPathbreakerPinNameOrWithPortName(circuit, CircuitSettings.getScanoutPort(),
                component, CircuitSettings.getScanoutPin());
    }

    private static void connectScanoutPort(VisualCircuit circuit, VisualContact contact) {
        String scanoutPortName = CircuitSettings.getScanoutPort();
        VisualContact scanoutPort = CircuitUtils.getOrCreatePort(circuit, scanoutPortName,
                Contact.IOType.OUTPUT, VisualContact.Direction.EAST);

        if (needsBuffering(circuit, contact)) {
            contact = addBuffering(circuit, contact);
        }
        CircuitUtils.connectIfPossible(circuit, contact, scanoutPort);
        SpaceUtils.positionPort(circuit, scanoutPort, true);
    }

    private static boolean needsBuffering(VisualCircuit circuit, VisualContact contact) {
        if (contact == null) {
            return false;
        }
        if (contact.isPort() && contact.isInput()) {
            return true;
        }
        return CircuitUtils.getDrivenOutputPort(circuit.getMathModel(), contact.getReferencedComponent()) != null;
    }

    private static VisualContact addBuffering(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent bufferComponent = GateUtils.createBufferGate(circuit);
        Point2D pos = contact.getRootSpacePosition();
        bufferComponent.setRootSpacePosition(new Point2D.Double(pos.getX() + 1.0, pos.getY() + 0.5));
        CircuitUtils.connectIfPossible(circuit, contact, bufferComponent.getFirstVisualInput());
        VisualFunctionContact outputContact = bufferComponent.getGateOutput();
        outputContact.setInitToOne(contact.getInitToOne());
        return outputContact;
    }

    private static void connectExistingScanChain(VisualCircuit circuit, VisualContact dataContact,
            Set<VisualContact> drivenContacts) {

        for (VisualContact outContact : drivenContacts) {
            CircuitUtils.connectIfPossible(circuit, dataContact, outContact);
        }
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

    public static boolean isScanInputPortName(String portName) {
        return MatchingUtils.isMatchingExactOrBus(portName, CircuitSettings.getScaninPort())
               || MatchingUtils.isMatchingExactOrBus(portName, CircuitSettings.getScanenPort())
               || MatchingUtils.isMatchingExact(portName, CircuitSettings.getScanckPort())
               || MatchingUtils.isMatchingExact(portName, CircuitSettings.getScantmPort());
    }

    public static boolean isScanOutputPortName(String portName) {
        String scanoutPort = CircuitSettings.getScanoutPort();
        return MatchingUtils.isMatchingExactOrBus(portName, scanoutPort);
    }

    public static Set<String> getScanoutAndAuxiliarySignals(Circuit circuit) {
        return circuit.getOutputPorts().stream()
                .map(Contact::getName)
                .filter(name -> isScanOutputPortName(name) || CircuitSettings.isAuxiliaryPortName(name))
                .collect(Collectors.toSet());
    }

}
