package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.DefaultNameManager;
import org.workcraft.plugins.circuit.Contact;

public class CircuitNameManager extends DefaultNameManager {

    @Override
    public String getPrefix(Node node) {
        if (node instanceof Contact) {
            Contact contact = (Contact) node;
            switch (contact.getIOType()) {
            case INPUT: return contact.isPort() ? "in" : "i";
            case OUTPUT: return contact.isPort() ? "out" : "o";
            }
        }
        return super.getPrefix(node);
    }

}
