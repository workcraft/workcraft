package org.workcraft.plugins.circuit.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.Identifier;
import org.workcraft.dom.references.NameManager;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.serialisation.References;

public class CircuitReferenceManager extends HierarchyReferenceManager {

    public CircuitReferenceManager(References refs) {
        super(refs);
    }

    @Override
    protected CircuitNameManager createNameManager() {
        return new CircuitNameManager();
    }

    @Override
    public String getName(Node node) {
        NameManager mgr = getNameManager(getNamespaceProvider(node));
        if ((node instanceof Contact) && !mgr.isNamed(node)) {
            return ((Contact) node).getName();
        }
        if ((node instanceof FunctionComponent) && !mgr.isNamed(node)) {
            return ((FunctionComponent) node).getName();
        }
        if (!mgr.isNamed(node)) {
            return null;
        }
        return super.getName(node);
    }

    @Override
    public void setName(Node node, String name) {
        super.setName(node, name);
        // support for the older models
        if (Identifier.isNumber(name) && (node instanceof Contact)) {
            String nodeName = ((Contact) node).getName();
            if ((nodeName != null) && !nodeName.isEmpty()) {
                name = nodeName;
            }
        } else if (Identifier.isNumber(name) && (node instanceof CircuitComponent)) {
            String nodeName = ((CircuitComponent) node).getName();
            if ((nodeName != null) && !nodeName.isEmpty()) {
                name = nodeName;
            }
        } else if (Identifier.isNumber(name)) {
            name = getNameManager(null).getPrefix(node) + name;
        }
        if (node instanceof Contact) {
            // propagate info to the contact itself
            ((Contact) node).setName(name);
        }
        if (node instanceof CircuitComponent) {
            // propagate info to the component itself
            ((CircuitComponent) node).setName(name);
        }
    }

}
