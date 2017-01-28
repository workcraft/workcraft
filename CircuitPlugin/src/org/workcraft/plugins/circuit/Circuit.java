package org.workcraft.plugins.circuit;

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.references.CircuitReferenceManager;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Identifier;

public class Circuit extends AbstractMathModel {

    public Circuit() {
        this(null, null);
    }

    public Circuit(MathGroup root) {
        this(root, null);
    }

    public Circuit(Container root, References refs) {
        super(root, new CircuitReferenceManager((NamespaceProvider) root, refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof CircuitComponent) return "g";
                if (node instanceof Contact) {
                    Contact contact = (Contact) node;
                    if (contact.getIOType() == IOType.INPUT) {
                        if (contact.getParent() instanceof CircuitComponent) return "i";
                        else return "in";
                    }
                    if (contact.getIOType() == IOType.OUTPUT) {
                        if (contact.getParent() instanceof CircuitComponent) return "z";
                        else return "out";
                    }
                }
                if (node instanceof Joint) return Identifier.createInternal("joint");
                return super.getPrefix(node);
            }
        });

        new FunctionConsistencySupervisor(this).attach(getRoot());
        new InitStateConsistencySupervisor(this).attach(getRoot());
        new ZeroDelayConsistencySupervisor(this).attach(getRoot());
        new IOTypeConsistencySupervisor(this).attach(getRoot());
    }

    public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
        MathConnection connection = new MathConnection((MathNode) first, (MathNode) second);
        Container container = Hierarchy.getNearestContainer(first, second);
        if (container instanceof CircuitComponent) {
            container = (Container) container.getParent();
        }
        if ((first instanceof Contact) && (second instanceof Contact)) {
            Contact firstContact = (Contact) first;
            Contact secondContact = (Contact) second;
            secondContact.setInitToOne(firstContact.getInitToOne());
        }
        container.add(connection);
        return connection;
    }

    public Collection<FunctionContact> getFunctionContacts() {
        return Hierarchy.getDescendantsOfType(getRoot(), FunctionContact.class);
    }

    public Collection<FunctionComponent> getFunctionComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), FunctionComponent.class);
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node != null) {
            if (node instanceof Joint) {
                properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
            }
        }
        return properties;
    }

    public Collection<Contact> getPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, new Func<Contact, Boolean>() {
            @Override
            public Boolean eval(Contact arg) {
                return arg.isPort();
            }
        });
    }

    public Collection<Contact> getDrivers() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, new Func<Contact, Boolean>() {
            @Override
            public Boolean eval(Contact arg) {
                return arg.isDriver();
            }
        });
    }

}
