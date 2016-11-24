package org.workcraft.plugins.circuit;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;

public class InitStateConsistencySupervisor extends StateSupervisor {

    private final Circuit circuit;

    public InitStateConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            String propertyName = pce.getPropertyName();
            if (sender instanceof Contact) {
                Contact contact = (Contact) sender;
                if (propertyName.equals(Contact.PROPERTY_INIT_TO_ONE)) {
                    handleInitToOneChange(contact);
                }
                if (propertyName.equals(Contact.PROPERTY_FORCED_INIT)) {
                    handleForcedInitChange(contact);
                }
            }
        }
    }

    private void handleInitToOneChange(Contact contact) {
        boolean initToOne = contact.getInitToOne();
        Node parent = contact.getParent();
        boolean isZeroDelay = false;
        boolean invertDriver = false;
        boolean inverDriven = false;
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;
            isZeroDelay = component.getIsZeroDelay();
            if (isZeroDelay && component.isInverter()) {
                invertDriver = contact.isOutput();
                inverDriven = contact.isInput();
            }
        }
        Contact driverContact = CircuitUtils.findDriver(circuit, contact, isZeroDelay);
        if (driverContact != null) {
            driverContact.setInitToOne(initToOne != invertDriver);
        }
        Collection<Contact> drivenContacts = CircuitUtils.findDriven(circuit, contact, isZeroDelay);
        for (Contact drivenContact: drivenContacts) {
            drivenContact.setInitToOne(initToOne != inverDriven);
        }
    }

    private void handleForcedInitChange(Contact contact) {
        boolean initialised = contact.getForcedInit();
        Contact driverContact = CircuitUtils.findDriver(circuit, contact, false);
        if (driverContact != null) {
            driverContact.setForcedInit(initialised);
        }
        Collection<Contact> drivenContacts = CircuitUtils.findDriven(circuit, contact, false);
        for (Contact drivenContact: drivenContacts) {
            drivenContact.setForcedInit(initialised);
        }
    }

}
