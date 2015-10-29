package org.workcraft.plugins.circuit;

import java.util.Collection;

import org.workcraft.dom.Node;
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
			String propertyName = pce.getPropertyName();
			if ((sender instanceof Contact) && propertyName.equals(Contact.PROPERTY_INIT_TO_ONE)) {
				Contact contact = (Contact)sender;
				handleInitStateChange(contact);
			}
		}
	}

	private void handleInitStateChange(Contact contact) {
		boolean initToOne = contact.getInitToOne();
		{
			Contact driverContact = CircuitUtils.findDriver(circuit, contact);
			if (driverContact != null) {
				boolean isZeroDelayInverter = false;
				Node parent = contact.getParent();
				if (parent instanceof FunctionComponent) {
					FunctionComponent component = (FunctionComponent)parent;
					isZeroDelayInverter = component.getIsZeroDelay() && component.isInverter();
				}
				driverContact.setInitToOne(initToOne != isZeroDelayInverter);
			}
		}
		{
			Collection<Contact> drivenContacts = CircuitUtils.findDriven(circuit, contact);
			for (Contact drivenContact: drivenContacts) {
				boolean isDrivenZeroDelayInverter = false;
				Node drivenParent = drivenContact.getParent();
				if (drivenParent instanceof FunctionComponent) {
					FunctionComponent drivenComponent = (FunctionComponent)drivenParent;
					isDrivenZeroDelayInverter = drivenComponent.getIsZeroDelay() && drivenComponent.isInverter();
				}
				drivenContact.setInitToOne(initToOne != isDrivenZeroDelayInverter);
			}
		}
	}

}
