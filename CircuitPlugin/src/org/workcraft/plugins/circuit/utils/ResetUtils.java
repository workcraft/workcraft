package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.plugins.circuit.*;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class ResetUtils {

    public static HashSet<Contact> setForceInit(Circuit circuit, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : circuit.getFunctionContacts()) {
            if (contact.isDriver() && contact.getForcedInit() != value) {
                contact.setForcedInit(value);
                result.add(contact);
            }
        }
        return result;
    }

    public static HashSet<Contact> setForceInitInputPorts(Circuit circuit, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact port : circuit.getInputPorts()) {
            if (port.getForcedInit() != value) {
                port.setForcedInit(value);
                result.add(port);
            }
        }
        return result;
    }

    public static HashSet<Contact> toggleForceInitInputPorts(Circuit circuit) {
        boolean allForceInit = true;
        for (Contact port : circuit.getInputPorts()) {
            if (!port.getForcedInit()) {
                allForceInit = false;
                break;
            }
        }
        return setForceInitInputPorts(circuit, !allForceInit);
    }

    public static HashSet<Contact> setForceInitSelfLoops(Circuit circuit, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : getSelfLoopContacts(circuit)) {
            if (contact.getForcedInit() != value) {
                contact.setForcedInit(value);
                result.add(contact);
            }
        }
        return result;
    }

    public static HashSet<Contact> toggleForceInitSelfLoops(Circuit circuit) {
        boolean allForceInit = true;
        for (Contact contact : getSelfLoopContacts(circuit)) {
            if (!contact.getForcedInit()) {
                allForceInit = false;
                break;
            }
        }
        return setForceInitSelfLoops(circuit, !allForceInit);
    }

    private static HashSet<Contact> getSelfLoopContacts(Circuit circuit) {
        HashSet<Contact> result = new HashSet<>();
        for (CircuitComponent component : circuit.getFunctionComponents()) {
            if ((component instanceof FunctionComponent) && ((FunctionComponent) component).getIsZeroDelay()) continue;
            for (Contact outputContact : component.getOutputs()) {
                for (CircuitComponent succComponent : StructureUtilsKt.getPostsetComponents(circuit, outputContact, true)) {
                    if (component != succComponent) continue;
                    result.add(outputContact);
                }
            }
        }
        return result;
    }

    public static HashSet<Contact> setForceInitSequentialGates(Circuit circuit, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : getSequentialGateContacts(circuit)) {
            if (contact.getForcedInit() != value) {
                contact.setForcedInit(value);
                result.add(contact);
            }
        }
        return result;
    }

    public static HashSet<Contact> toggleForceInitSequentialGates(Circuit circuit) {
        boolean allForceInit = true;
        for (FunctionContact contact : getSequentialGateContacts(circuit)) {
            allForceInit &= contact.getForcedInit();
        }
        return setForceInitSequentialGates(circuit, !allForceInit);
    }

    private static HashSet<FunctionContact> getSequentialGateContacts(Circuit circuit) {
        HashSet<FunctionContact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.isGate()) {
                for (FunctionContact contact : component.getFunctionOutputs()) {
                    if ((contact.getSetFunction() == null) || (contact.getResetFunction() == null)) continue;
                    result.add(contact);
                }
            }
        }
        return result;
    }

    public static HashSet<FunctionContact> untagRedundantForceInitPins(Circuit circuit) {
        HashSet<FunctionContact> userForceInitContacts = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver() && contact.getForcedInit()) {
                userForceInitContacts.add(contact);
            }
        }
        return clearRedundantForceInitPins(circuit, userForceInitContacts);
    }

    public static HashSet<FunctionContact> tagNecessaryForceInitPins(Circuit circuit) {
        HashSet<FunctionContact> addedForceInitContacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getIsZeroDelay()) continue;
            for (FunctionContact contact : component.getFunctionContacts()) {
                if (contact.isPin() && contact.isDriver() && !contact.getForcedInit()) {
                    contact.setForcedInit(true);
                    addedForceInitContacts.add(contact);
                }
            }
        }
        return clearRedundantForceInitPins(circuit, addedForceInitContacts);
    }

    private static HashSet<FunctionContact> clearRedundantForceInitPins(Circuit circuit, HashSet<FunctionContact> contacts) {
        HashSet<FunctionContact> result = new HashSet<>();
        for (FunctionContact contact : contacts) {
            contact.setForcedInit(false);
            InitialisationState initState = new InitialisationState(circuit);
            if (initState.isCorrectlyInitialised(contact)) {
                result.add(contact);
            } else {
                contact.setForcedInit(true);
            }
        }
        return result;
    }

    public static void insertReset(VisualCircuit circuit, String portName, boolean activeLow) {
        VisualFunctionContact resetPort = getOrCreateResetPort(circuit, portName);
        if (resetPort == null) {
            return;
        }
        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            VisualFunctionContact gaterOutput = component.getMainVisualOutput();
            if ((gaterOutput != null) && gaterOutput.getForcedInit()) {
                if (component.isBuffer()) {
                    resetBuffer(circuit, component, resetPort, activeLow);
                    continue;
                }
                if (component.isInverter()) {
                    resetInverter(circuit, component, resetPort, activeLow);
                    continue;
                }
            }

            boolean isSimpleGate = component.isGate() && (component.getVisualInputs().size() < 3);
            Collection<VisualFunctionContact> forceInitGateContacts = new HashSet<>();
            Collection<VisualFunctionContact> forceInitFuncContacts = new HashSet<>();
            for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
                if (contact.isOutput() && contact.isPin() && contact.getForcedInit()) {
                    if (isSimpleGate || contact.getReferencedContact().isSequential()) {
                        forceInitFuncContacts.add(contact);
                    } else {
                        forceInitGateContacts.add(contact);
                    }
                }
            }
            VisualContact resetContact = null;
            if (!forceInitFuncContacts.isEmpty()) {
                resetContact = component.createContact(Contact.IOType.INPUT);
                try {
                    circuit.connect(resetPort, resetContact);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
            for (VisualFunctionContact contact : forceInitFuncContacts) {
                insertResetFunction(contact, resetContact, activeLow);
                component.setLabel("");
            }
            for (VisualFunctionContact contact : forceInitGateContacts) {
                insertResetGate(circuit, resetPort, contact, activeLow);
            }
        }
        positionResetPort(circuit, resetPort);
        forceInitResetCircuit(circuit, resetPort, activeLow);
    }

    private static VisualFunctionContact getOrCreateResetPort(VisualCircuit circuit, String portName) {
        VisualFunctionContact result = null;
        VisualComponent component = circuit.getVisualComponentByMathReference(portName, VisualComponent.class);
        if (component == null) {
            result = circuit.getOrCreatePort(portName, Contact.IOType.INPUT);
            if (result == null) {
                DialogUtils.showError("Cannot create reset port '" + portName + "'.");
                return null;
            }
        } else if (component instanceof VisualFunctionContact) {
            result = (VisualFunctionContact) component;
            if (result.isOutput()) {
                DialogUtils.showError("Cannot reuse existing output port '" + portName + "' for circuit reset.");
                return null;
            }
            DialogUtils.showWarning("Reusing existing input port '" + portName + "' for circuit reset.");
        } else {
            DialogUtils.showError("Cannot insert reset port '" + portName + "' because a component with the same name already exists.");
            return null;
        }
        return result;
    }

    private static void resetBuffer(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact resetPort, boolean activeLow) {

        VisualFunctionContact outputContact = component.getFirstVisualOutput();
        boolean initToOne = outputContact.getInitToOne();
        Gate3 gate;
        if (activeLow) {
            gate = initToOne ? CircuitSettings.parseNandbData() : CircuitSettings.parseAndData();
        } else {
            gate = initToOne ? CircuitSettings.parseOrData() : CircuitSettings.parseNorbData();
        }
        VisualFunctionContact inputContact = component.getFirstVisualInput();
        // Temporary rename gate output, so there is no name clash on renaming gate input
        circuit.setMathName(outputContact, Identifier.createInternal(gate.out));
        circuit.setMathName(inputContact, gate.in1);
        circuit.setMathName(outputContact, gate.out);
        VisualFunctionContact resetContact = circuit.getOrCreateContact(component, gate.in2, Contact.IOType.INPUT);
        try {
            circuit.connect(resetPort, resetContact);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }

        Contact firstVar = inputContact.getReferencedContact();
        Contact secondVar = resetContact.getReferencedContact();
        BooleanFormula func;
        if (activeLow) {
            if (initToOne) {
                func = BooleanOperations.nandb(firstVar, secondVar, new CleverBooleanWorker());
            } else {
                func = BooleanOperations.and(firstVar, secondVar, new CleverBooleanWorker());
            }
        } else {
            if (initToOne) {
                func = BooleanOperations.or(firstVar, secondVar, new CleverBooleanWorker());
            } else {
                func = BooleanOperations.norb(firstVar, secondVar, new CleverBooleanWorker());
            }
        }
        outputContact.setSetFunction(func);
        component.setLabel(gate.name);
    }

    private static void resetInverter(VisualCircuit circuit, VisualFunctionComponent component,
            VisualFunctionContact resetPort, boolean activeLow) {

        VisualFunctionContact outputContact = component.getFirstVisualOutput();
        boolean initToOne = outputContact.getInitToOne();
        Gate3 gate;
        if (activeLow) {
            gate = initToOne ? CircuitSettings.parseNandData() : CircuitSettings.parseNorbData();
        } else {
            gate = initToOne ? CircuitSettings.parseNandbData() : CircuitSettings.parseNorData();
        }
        VisualFunctionContact inputContact = component.getFirstVisualInput();
        // Temporary rename gate output, so there is no name clash on renaming gate input
        circuit.setMathName(outputContact, Identifier.createInternal(gate.out));
        circuit.setMathName(inputContact, gate.in2);
        circuit.setMathName(outputContact, gate.out);
        VisualFunctionContact resetContact = circuit.getOrCreateContact(component, gate.in1, Contact.IOType.INPUT);
        try {
            circuit.connect(resetPort, resetContact);
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }

        Contact firstVar = resetContact.getReferencedContact();
        Contact secondVar = inputContact.getReferencedContact();
        BooleanFormula func;
        if (activeLow) {
            if (initToOne) {
                func = BooleanOperations.nand(firstVar, secondVar, new CleverBooleanWorker());
            } else {
                func = BooleanOperations.norb(firstVar, secondVar, new CleverBooleanWorker());
            }
        } else {
            if (initToOne) {
                func = BooleanOperations.nandb(firstVar, secondVar, new CleverBooleanWorker());
            } else {
                func = BooleanOperations.nor(firstVar, secondVar, new CleverBooleanWorker());
            }
        }
        outputContact.setSetFunction(func);
        component.setLabel(gate.name);
    }

    private static void insertResetFunction(VisualFunctionContact contact, VisualContact resetContact, boolean activeLow) {
        BooleanFormula setFunction = contact.getSetFunction();
        BooleanFormula resetFunction = contact.getResetFunction();
        Contact resetVar = resetContact.getReferencedContact();
        if (activeLow) {
            if (contact.getInitToOne()) {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.or(BooleanOperations.not(resetVar), setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.and(resetVar, resetFunction));
                }
            } else {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.and(resetVar, setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.or(BooleanOperations.not(resetVar), resetFunction));
                }
            }
        } else {
            if (contact.getInitToOne()) {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.or(resetVar, setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.and(BooleanOperations.not(resetVar), resetFunction));
                }
            } else {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.and(BooleanOperations.not(resetVar), setFunction));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.or(resetVar, resetFunction));
                }
            }
        }
    }

    private static void insertResetGate(VisualCircuit circuit, VisualContact resetPort, VisualFunctionContact contact, boolean activeLow) {
        // Change connection scale mode to LOCK_RELATIVELY for cleaner relocation of components
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class);
        HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap
                = ConnectionUtils.replaceConnectionScaleMode(connections, VisualConnection.ScaleMode.LOCK_RELATIVELY);

        double gateSpace = 3.0;
        SpaceUtils.makeSpaceAfterContact(circuit, contact, gateSpace + 1.0);
        VisualJoint joint = CircuitUtils.detachJoint(circuit, contact);
        if (joint != null) {
            joint.setRootSpacePosition(getOffsetContactPosition(contact, gateSpace));
        }
        // Restore connection scale mode
        ConnectionUtils.restoreConnectionScaleMode(connectionToScaleModeMap);

        VisualFunctionComponent resetGate = createResetGate(circuit, contact.getInitToOne(), activeLow);
        GateUtils.insertGateAfter(circuit, resetGate, contact);
        connectHangingInputs(circuit, resetPort, resetGate);
        GateUtils.propagateInitialState(circuit, resetGate);
    }

    private static VisualFunctionComponent createResetGate(VisualCircuit circuit, boolean initToOne, boolean activeLow) {
        if (activeLow) {
            return initToOne ? GateUtils.createNandbGate(circuit) : GateUtils.createAndGate(circuit);
        } else {
            return initToOne ? GateUtils.createOrGate(circuit) : GateUtils.createNorbGate(circuit);
        }
    }

    private static Point2D getOffsetContactPosition(VisualContact contact, double space) {
        double d = contact.isPort() ? -space : space;
        double x = contact.getRootSpaceX() + d * contact.getDirection().getGradientX();
        double y = contact.getRootSpaceY() + d * contact.getDirection().getGradientY();
        return new Point2D.Double(x, y);
    }

    private static void connectHangingInputs(VisualCircuit circuit, VisualContact port, VisualFunctionComponent component) {
        for (VisualContact contact : component.getVisualInputs()) {
            if (!circuit.getPreset(contact).isEmpty()) continue;
            try {
                circuit.connect(port, contact);
            } catch (InvalidConnectionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void forceInitResetCircuit(VisualCircuit circuit, VisualFunctionContact resetPort, boolean activeLow) {
        resetPort.setInitToOne(!activeLow);
        resetPort.setForcedInit(true);
        resetPort.setSetFunction(activeLow ? One.instance() : Zero.instance());
        resetPort.setResetFunction(activeLow ? Zero.instance() : One.instance());
        for (VisualFunctionContact contact : circuit.getVisualFunctionContacts()) {
            if (contact.isPin() && contact.isOutput()) {
                contact.setForcedInit(false);
            }
        }
    }

    private static void positionResetPort(VisualCircuit circuit, VisualFunctionContact resetPort) {
        Collection<Touchable> nodes = new HashSet<>();
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualConnection.class));
        for (VisualConnection resetConnection : circuit.getConnections(resetPort)) {
            nodes.remove(resetConnection);
        }
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualCircuitComponent.class));
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualJoint.class));
        Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
        Collection<VisualContact> driven = CircuitUtils.findDriven(circuit, resetPort, false);
        double x = modelBox.getMinX();
        double y = driven.isEmpty() ? modelBox.getCenterY() : MixUtils.middleRootspacePosition(driven).getY();
        Point2D.Double pos = new Point2D.Double(TransformHelper.snapP5(x), TransformHelper.snapP5(y));
        resetPort.setRootSpacePosition(pos);

        VisualJoint joint = CircuitUtils.detachJoint(circuit, resetPort);
        if (joint != null) {
            joint.setRootSpacePosition(getOffsetContactPosition(resetPort, 0.5));
        }
    }

}
