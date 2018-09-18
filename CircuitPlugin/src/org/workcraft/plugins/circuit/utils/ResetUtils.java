package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.plugins.circuit.*;
import org.workcraft.util.Hierarchy;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class ResetUtils {

    public static void insertReset(VisualCircuit circuit, String portName, boolean activeLow) {
        VisualFunctionContact resetPort = circuit.getOrCreateContact(null, portName, Contact.IOType.INPUT);

        for (VisualFunctionComponent component : circuit.getVisualFunctionComponents()) {
            boolean needsResetPin = false;
            Collection<VisualFunctionContact> forceInitOutputContacts = new HashSet<>();
            for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
                if (contact.isOutput() && contact.isPin() && contact.getForcedInit()) {
                    forceInitOutputContacts.add(contact);
                    needsResetPin |= contact.getReferencedFunctionContact().isSequential();
                }
            }
            VisualContact resetContact = null;
            if (needsResetPin) {
                resetContact = circuit.getOrCreateContact(component, portName, Contact.IOType.INPUT);
                try {
                    circuit.connect(resetPort, resetContact);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
            for (VisualFunctionContact contact : forceInitOutputContacts) {
                if (contact.getReferencedFunctionContact().isSequential()) {
                    insertResetFunction(contact, resetContact, activeLow);
                    component.setLabel("");
                } else {
                    insertResetGate(circuit, resetPort, contact, activeLow);
                }
            }
        }

        for (VisualFunctionContact contact : circuit.getVisualFunctionContacts()) {
            if (contact.isPin() && contact.isOutput()) {
                contact.setForcedInit(false);
            }
        }
        resetPort.setInitToOne(!activeLow);
        resetPort.setForcedInit(true);
        resetPort.setSetFunction(activeLow ? One.instance() : Zero.instance());
        resetPort.setResetFunction(activeLow ? Zero.instance() : One.instance());

        Collection<Touchable> nodes = new HashSet<>();
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualConnection.class));
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualCircuitComponent.class));
        nodes.addAll(Hierarchy.getChildrenOfType(circuit.getRoot(), VisualJoint.class));
        Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
        resetPort.setRootSpacePosition(new Point2D.Double(modelBox.getMinX(), modelBox.getMinY()));
    }

    private static void insertResetFunction(VisualFunctionContact contact, VisualContact resetContact, boolean activeLow) {
        BooleanFormula setFunction = contact.getSetFunction();
        BooleanFormula resetFunction = contact.getResetFunction();
        Contact resetVar = resetContact.getReferencedContact();
        CleverBooleanWorker worker = new CleverBooleanWorker();
        if (activeLow) {
            if (contact.getInitToOne()) {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.or(BooleanOperations.not(resetVar), setFunction, worker));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.and(resetVar, resetFunction, worker));
                }
            } else {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.and(resetVar, setFunction, worker));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.or(BooleanOperations.not(resetVar), resetFunction, worker));
                }
            }
        } else {
            if (contact.getInitToOne()) {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.or(resetVar, setFunction, worker));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.and(BooleanOperations.not(resetVar), resetFunction, worker));
                }
            } else {
                if (setFunction != null) {
                    contact.setSetFunction(BooleanOperations.and(BooleanOperations.not(resetVar), setFunction, worker));
                }
                if (resetFunction != null) {
                    contact.setResetFunction(BooleanOperations.or(resetVar, resetFunction, worker));
                }
            }
        }
    }

    private static void insertResetGate(VisualCircuit circuit, VisualContact resetPort, VisualFunctionContact contact, boolean activeLow) {
        // Change connection scale mode to LOCK_RELATIVELY for cleaner relocation of components
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualConnection.class);
        HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap
                = ConnectionUtils.replaceConnectionScaleMode(connections, VisualConnection.ScaleMode.LOCK_RELATIVELY);

        SpaceUtils.makeSpaceAfterContact(circuit, contact, 4.0);
        VisualJoint joint = CircuitUtils.detachJoint(circuit, contact);
        VisualConnection connection = (VisualConnection) circuit.getConnection(contact, joint);
        connection.setScaleMode(VisualConnection.ScaleMode.LOCK_RELATIVELY);
        joint.setRootSpacePosition(getOffsetContactPosition(contact, 3.0));

        // Restore connection scale mode
        connection.setScaleMode(VisualConnection.ScaleMode.NONE);
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
        double x = contact.getRootSpaceX() + space * contact.getDirection().getGradientX();
        double y = contact.getRootSpaceY() + space * contact.getDirection().getGradientY();
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

}
