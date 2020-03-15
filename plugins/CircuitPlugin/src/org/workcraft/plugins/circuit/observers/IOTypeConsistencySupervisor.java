package org.workcraft.plugins.circuit.observers;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.Contact.IOType;

public class IOTypeConsistencySupervisor extends StateSupervisor {

    private final Circuit circuit;

    public IOTypeConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            String propertyName = pce.getPropertyName();
            if ((sender instanceof Contact) && propertyName.equals(Contact.PROPERTY_IO_TYPE)) {
                handleIOTypeChange((Contact) sender);
            }
        }
    }

    private void handleIOTypeChange(Contact contact) {
        if (contact.isPort()) {
            if (contact.isInput()) {
                if (!circuit.getPreset(contact).isEmpty()) {
                    contact.setIOType(IOType.OUTPUT);
                    throw new ArgumentException("Primary input cannot be driven.");
                }
            } else {
                if (!circuit.getPostset(contact).isEmpty()) {
                    contact.setIOType(IOType.INPUT);
                    throw new ArgumentException("Primary ouput cannot be a driver.");
                }
            }
        } else {
            if (contact.isInput()) {
                if (!circuit.getPostset(contact).isEmpty()) {
                    contact.setIOType(IOType.OUTPUT);
                    throw new ArgumentException("Input pin of a component cannot be a driver.");
                }
            } else {
                if (!circuit.getPreset(contact).isEmpty()) {
                    contact.setIOType(IOType.INPUT);
                    throw new ArgumentException("Output pin of a component cannot be driven.");
                }
            }
        }
    }

}
