package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.DefaultNameManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.types.TwoWayMap;
import org.workcraft.utils.DialogUtils;

public class CircuitNameManager extends DefaultNameManager {

    private final TwoWayMap<String, Node> signalMap = new TwoWayMap<>();

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
    public void setName(Node node, String name, boolean force) {
        if (isSignalNode(node)) {
            if (node instanceof Contact) {
                Contact contact = (Contact) node;
                if (isUnusedName(name) || renameOccupantIfDifferent(contact, name)) {
                    setSignalName(contact, name);
                    contact.setName(name);
                }
            } else {
                if (!isUnusedName(name) && (node != getNode(name))) {
                    name = getDerivedName(node, name);
                }
                setSignalName(node, name);
            }
        } else {
            super.setName(node, name, force);
        }
    }

    @Override
    public String getName(Node node) {
        if (isSignalNode(node)) {
            String signalName = getSignalName(node);
            return (signalName != null) && (node instanceof NamespaceProvider)
                    ? Identifier.appendNamespaceSeparator(signalName) : signalName;
        } else {
            return super.getName(node);
        }
    }

    @Override
    public Node getNode(String name) {
        Node result = getSignalNode(name);
        if (result == null) {
            result = super.getNode(name);
        }
        return result;
    }

    @Override
    public boolean isUnusedName(String name) {
        return (getSignalNode(name) == null)
                && super.isUnusedName(Identifier.truncateNamespaceSeparator(name))
                && super.isUnusedName(Identifier.appendNamespaceSeparator(name));
    }

    private boolean renameOccupantIfDifferent(Node node, String name) {
        Node occupant = getNode(name);
        if ((occupant != null) && (occupant.getParent() != null) && (occupant != node)) {
            if (!(occupant instanceof CircuitComponent)) {
                throw new ArgumentException("Name '" + name + "' is unavailable.");
            }
            String derivedName = getDerivedName(occupant, name);
            String msg = "Name '" + name + "' is already taken by a component.\n" +
                    "Rename that component to '" + derivedName + "' and continue?";

            if (!DialogUtils.showConfirmWarning(msg)) {
                return false;
            }
            setName(occupant, derivedName, true);
        }
        return true;
    }

    @Override
    public void setDefaultNameIfUnnamed(Node node) {
        if (isSignalNode(node)) {
            if (getSignalName(node) == null) {
                String prefix = getPrefix(node);
                Integer count = getPrefixCount(prefix);
                String name;
                do {
                    name = Identifier.compose(prefix, (count++).toString());
                } while (getNode(name) != null);
                setSignalName(node, name);
            }
        } else {
            super.setDefaultNameIfUnnamed(node);
        }
    }

    private boolean isSignalNode(Node node) {
        return (node instanceof CircuitComponent) || (node instanceof Contact);
    }

    private String getSignalName(Node node) {
        return signalMap.getKey(node);
    }

    private Node getSignalNode(String name) {
        return name == null ? null : signalMap.getValue(Identifier.truncateNamespaceSeparator(name));
    }

    private void setSignalName(Node node, String name) {
        signalMap.put(Identifier.truncateNamespaceSeparator(name), node);
    }

}
