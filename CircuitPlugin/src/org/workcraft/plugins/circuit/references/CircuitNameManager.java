package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.DefaultNameManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.plugins.circuit.CircuitComponent;
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

    @Override
    public void setName(Node node, String name) {
        if (Identifier.isNumber(name)) {
            // Support for the older models.
            name = convertFromLegacy(node, name);
        }
        super.setName(node, name);
        propagateToComponents(node, name);
    }

    private String convertFromLegacy(Node node, String name) {
        String result = null;
        if (node instanceof Contact) {
            result = ((Contact) node).getName();
        }
        if (node instanceof CircuitComponent) {
            result = ((CircuitComponent) node).getName();
        }
        if ((result != null) && !result.isEmpty()) {
            result = getPrefix(node) + name;
        }
        return result;
    }

    private void propagateToComponents(Node node, String name) {
        if (node instanceof Contact) {
            ((Contact) node).setName(name);
        }
        if (node instanceof CircuitComponent) {
            ((CircuitComponent) node).setName(name);
        }
    }

}
