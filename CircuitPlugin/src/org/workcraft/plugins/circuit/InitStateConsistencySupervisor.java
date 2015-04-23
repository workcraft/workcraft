package org.workcraft.plugins.circuit;

import java.util.Collection;

import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;

public class InitStateConsistencySupervisor extends StateSupervisor  {

	private final Circuit circuit;

	public InitStateConsistencySupervisor(Circuit circuit) {
		this.circuit = circuit;
	}

	@Override
	public void handleEvent(StateEvent e) {
		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pce = (PropertyChangedEvent)e;
			Object sender = e.getSender();
			if ((sender instanceof Contact) && pce.getPropertyName().equals(Contact.PROPERTY_INIT_TO_ONE)) {
				Contact contact = (Contact)sender;
				handleInitStateChange(contact);
			}
		}
	}

	private void handleInitStateChange(Contact contact) {
		boolean initToOne = contact.getInitToOne();
		Contact driverContact = CircuitUtils.findDriver(circuit, contact);
		if (driverContact != null) {
			driverContact.setInitToOne(initToOne);
		}

		Collection<Contact> drivenContacts = CircuitUtils.findDriven(circuit, contact);
		for (Contact drivenContact: drivenContacts) {
			drivenContact.setInitToOne(initToOne);
		}
	}

}
