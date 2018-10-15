package org.workcraft.plugins.circuit;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.circuit.observers.FunctionConsistencySupervisor;
import org.workcraft.plugins.circuit.observers.IOTypeConsistencySupervisor;
import org.workcraft.plugins.circuit.observers.ZeroDelayConsistencySupervisor;
import org.workcraft.plugins.circuit.references.CircuitReferenceManager;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.MultiSet;

import java.util.Collection;
import java.util.HashSet;

public class Circuit extends AbstractMathModel {

    public Circuit() {
        this(null, null);
    }

    public Circuit(Container root, References refs) {
        super(root, new CircuitReferenceManager(refs));
        new FunctionConsistencySupervisor(this).attach(getRoot());
        new ZeroDelayConsistencySupervisor(this).attach(getRoot());
        new IOTypeConsistencySupervisor(this).attach(getRoot());
    }

    @Override
    public void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException {
        super.validateConnection(first, second);

        if (first == second) {
            throw new InvalidConnectionException("Connections are only valid between different objects.");
        }

        if (second instanceof MathConnection) {
            throw new InvalidConnectionException("Merging connections is not allowed.");
        }

        if ((second instanceof Contact) || (second instanceof Joint)) {
            for (Connection connection : getConnections(second)) {
                if ((connection.getFirst() != first) && (connection.getSecond() == second)) {
                    throw new InvalidConnectionException("Only one connection is allowed as a driver.");
                }
            }
        }

        if (first instanceof Contact) {
            Contact contact = (Contact) first;
            if (contact.isInput() && !contact.isPort()) {
                throw new InvalidConnectionException("Input pin of a component cannot be a driver.");
            }
            if (contact.isOutput() && contact.isPort()) {
                throw new InvalidConnectionException("Primary output cannot be a driver.");
            }
        }

        if (second instanceof Contact) {
            Contact contact = (Contact) second;
            if (contact.isOutput() && !contact.isPort()) {
                throw new InvalidConnectionException("Output pin of a component cannot be driven.");
            }
            if (contact.isInput() && contact.isPort()) {
                throw new InvalidConnectionException("Primary input cannot be driven.");
            }
        }

        HashSet<Contact> drivenSet = new HashSet<>();
        Contact driver = null;
        if (first instanceof MathConnection) {
            MathConnection firstConnection = (MathConnection) first;
            driver = CircuitUtils.findDriver(this, firstConnection, true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(this, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(this, firstConnection, true));
            }
        } else if ((first instanceof Contact) || (first instanceof Joint)) {
            driver = CircuitUtils.findDriver(this, first, true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(this, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(this, first, true));
            }
        }
        if ((second instanceof Contact) || (second instanceof Joint)) {
            drivenSet.addAll(CircuitUtils.findDriven(this, second, true));
        }
        int outputPortCount = 0;
        for (Contact driven: drivenSet) {
            if (driven.isOutput() && driven.isPort()) {
                outputPortCount++;
                if (outputPortCount > 1) {
                    throw new InvalidConnectionException("Fork on output ports is not allowed.");
                }
                if ((driver != null) && driver.isInput() && driver.isPort()) {
                    throw new InvalidConnectionException("Direct connection from input port to output port is not allowed.");
                }
            }
        }
        // Handle zero-delay components
        Node firstParent = first.getParent();
        if (firstParent instanceof FunctionComponent) {
            FunctionComponent firstComponent = (FunctionComponent) firstParent;
            Node secondParent = second.getParent();
            if (secondParent instanceof FunctionComponent) {
                FunctionComponent secondComponent = (FunctionComponent) secondParent;
                if (firstComponent.getIsZeroDelay() && secondComponent.getIsZeroDelay()) {
                    throw new InvalidConnectionException("Zero delay components cannot be connected to each other.");
                }
            }
            if (second instanceof Contact) {
                Contact secondContact = (Contact) second;
                if (firstComponent.getIsZeroDelay() && secondContact.isPort() && secondContact.isOutput()) {
                    throw new InvalidConnectionException("Zero delay components cannot be connected to output ports.");
                }
            }
        }
    }

    @Override
    public MathConnection connect(MathNode first, MathNode second) throws InvalidConnectionException {
        validateConnection(first, second);
        MathConnection connection = new MathConnection(first, second);
        Container container = Hierarchy.getNearestContainer(first, second);
        if (container instanceof CircuitComponent) {
            container = (Container) container.getParent();
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

    public Collection<Joint> getJoints() {
        return Hierarchy.getDescendantsOfType(getRoot(), Joint.class);
    }

    public Collection<Contact> getPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, contact -> contact.isPort());
    }

    public Collection<Contact> getInputPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, contact -> contact.isPort() && contact.isInput());
    }

    public Collection<Contact> getOutputPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, contact -> contact.isPort() && contact.isOutput());
    }

    public Collection<Contact> getDrivers() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, contact -> contact.isDriver());
    }

    @Override
    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        result.add("Component", getFunctionComponents().size());
        result.add("Port", getPorts().size());
        return result;
    }

}
