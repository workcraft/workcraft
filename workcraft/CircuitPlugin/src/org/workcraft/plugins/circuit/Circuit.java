package org.workcraft.plugins.circuit;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.references.Identifier;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.observers.FunctionConsistencySupervisor;
import org.workcraft.plugins.circuit.observers.IOTypeConsistencySupervisor;
import org.workcraft.plugins.circuit.observers.ZeroDelayConsistencySupervisor;
import org.workcraft.plugins.circuit.references.CircuitReferenceManager;
import org.workcraft.serialisation.NoAutoSerialisation;
import org.workcraft.serialisation.References;
import org.workcraft.types.MultiSet;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.Hierarchy;

import java.io.File;
import java.util.Collection;

public class Circuit extends AbstractMathModel {

    private FileReference environment = null;

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

    public boolean hasPort(String ref) {
        return getNodeByReference(null, ref) instanceof Contact;
    }

    public boolean hasPin(FunctionComponent component, String ref) {
        return getNodeByReference(component, ref) instanceof Contact;
    }

    public FunctionContact getPin(FunctionComponent component, String name) {
        for (FunctionContact contact : component.getFunctionContacts()) {
            String contactName = getName(contact);
            if (name.equals(contactName)) {
                return contact;
            }
        }
        return null;
    }

    public Collection<FunctionContact> getFunctionContacts() {
        return Hierarchy.getDescendantsOfType(getRoot(), FunctionContact.class);
    }

    public Collection<FunctionComponent> getFunctionComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), FunctionComponent.class);
    }

    public Collection<Joint> getJoints() {
        return Hierarchy.getDescendantsOfType(getRoot(), Joint.class);
    }

    public Collection<Contact> getPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, Contact::isPort);
    }

    public Collection<Contact> getPins() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, Contact::isPin);
    }

    public Collection<Contact> getInputPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, contact -> contact.isPort() && contact.isInput());
    }

    public Collection<Contact> getOutputPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, contact -> contact.isPort() && contact.isOutput());
    }

    public Collection<Contact> getDrivers() {
        return Hierarchy.getDescendantsOfType(getRoot(), Contact.class, Contact::isDriver);
    }

    @Override
    public MultiSet<String> getStatistics() {
        MultiSet<String> result = new MultiSet<>();
        result.add("Component", getFunctionComponents().size());
        result.add("Port", getPorts().size());
        return result;
    }

    public FileReference getEnvironment() {
        return environment;
    }

    public void setEnvironment(FileReference value) {
        environment = value;
    }

    @NoAutoSerialisation
    public File getEnvironmentFile() {
        return (environment == null) ? null : environment.getFile();
    }

    @NoAutoSerialisation
    public void setEnvironmentFile(File file) {
        setEnvironmentFile(FileUtils.getFullPath(file));
    }

    @NoAutoSerialisation
    public void setEnvironmentFile(String path) {
        if (environment == null) {
            environment = new FileReference();
        }
        environment.setPath(path);
    }

    public String getComponentName(CircuitComponent component) {
        String nodeName = getName(component);
        return nodeName == null ? null : Identifier.truncateNamespaceSeparator(nodeName);
    }

    public String getComponentReference(CircuitComponent component) {
        String nodeRef = getNodeReference(component);
        return nodeRef == null ? null : Identifier.truncateNamespaceSeparator(nodeRef);
    }

}
