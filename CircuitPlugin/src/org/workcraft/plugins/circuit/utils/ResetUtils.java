package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.*;
import org.workcraft.plugins.circuit.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ResetUtils {

    public static Set<Contact> tagForceInitClearAll(Circuit circuit) {
        return setForceInit(circuit.getFunctionContacts(), false);
    }

    public static Set<Contact> tagForceInitInputPorts(Circuit circuit) {
        return setForceInit(circuit.getInputPorts(), true);
    }

    public static Set<Contact> tagForceInitSelfloopGates(Circuit circuit) {
        HashSet<Contact> contacts = new HashSet<>();
        for (CircuitComponent component : circuit.getFunctionComponents()) {
            if ((component instanceof FunctionComponent) && ((FunctionComponent) component).getIsZeroDelay()) continue;
            for (Contact outputContact : component.getOutputs()) {
                for (CircuitComponent succComponent : StructureUtilsKt.getPostsetComponents(circuit, outputContact, true)) {
                    if (component != succComponent) continue;
                    contacts.add(outputContact);
                }
            }
        }
        return setForceInit(contacts, true);
    }

    public static Set<Contact> tagForceInitSequentialPins(Circuit circuit) {
        Set<Contact> contacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.isGate()) {
                for (FunctionContact contact : component.getFunctionOutputs()) {
                    if ((contact.getSetFunction() != null) && (contact.getResetFunction() != null)) {
                        contacts.add(contact);
                    }
                }
            }
        }
        return setForceInit(contacts, true);
    }

    public static Set<Contact> tagForceInitAutoAppend(Circuit circuit) {
        Set<Contact> contacts = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (!component.getIsZeroDelay()) {
                for (FunctionContact contact : component.getFunctionContacts()) {
                    if (contact.isPin() && contact.isDriver()) {
                        contacts.add(contact);
                    }
                }
            }
        }
        Set<Contact> changedContacts = setForceInit(contacts, true);
        return simplifyForceInit(circuit, changedContacts);
    }

    private static Set<Contact> setForceInit(Collection<? extends Contact> contacts, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
            if (contact.getForcedInit() != value) {
                contact.setForcedInit(value);
                result.add(contact);
            }
        }
        return result;
    }

    public static Set<Contact> tagForceInitAutoDiscard(Circuit circuit) {
        HashSet<FunctionContact> userForceInitContacts = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver() && contact.getForcedInit()) {
                userForceInitContacts.add(contact);
            }
        }
        return simplifyForceInit(circuit, userForceInitContacts);
    }

    private static Set<Contact> simplifyForceInit(Circuit circuit, Collection<? extends Contact> contacts) {
        Set<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
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
        VisualFunctionContact resetPort = CircuitUtils.getOrCreatePort(circuit, portName, Contact.IOType.INPUT);
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
        SpaceUtils.positionPort(circuit, resetPort);
        forceInitResetCircuit(circuit, resetPort, activeLow);
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
        SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
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

}
