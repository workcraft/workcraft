package org.workcraft.plugins.circuit;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;

public class InitialisedConsistencySupervisor extends StateSupervisor  {

    private final Circuit circuit;

    public InitialisedConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            String propertyName = pce.getPropertyName();
            if ((sender instanceof Contact) && propertyName.equals(Contact.PROPERTY_INITIALISED)) {
                Contact contact = (Contact) sender;
                handleInitialisedChange(contact);
            }
        }
    }

    private void handleInitialisedChange(Contact contact) {
        boolean initialised = contact.getInitialised();
        Node parent = contact.getParent();
        boolean isZeroDelay = false;
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;
            isZeroDelay = component.getIsZeroDelay();
        }
        Contact driverContact = CircuitUtils.findDriver(circuit, contact, isZeroDelay);
        if (driverContact != null) {
            driverContact.setInitialised(initialised);
        }
        Collection<Contact> drivenContacts = CircuitUtils.findDriven(circuit, contact, isZeroDelay);
        for (Contact drivenContact: drivenContacts) {
            drivenContact.setInitialised(initialised);
        }
    }

}
