package org.workcraft.plugins.circuit;

import org.workcraft.Framework;
import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.commands.CircuitLayoutCommand;
import org.workcraft.plugins.circuit.commands.CircuitLayoutSettings;
import org.workcraft.plugins.circuit.propertydescriptors.EnvironmentFilePropertyDescriptor;
import org.workcraft.plugins.circuit.propertydescriptors.ResetFunctionPropertyDescriptor;
import org.workcraft.plugins.circuit.propertydescriptors.SetFunctionPropertyDescriptor;
import org.workcraft.plugins.circuit.routing.RouterClient;
import org.workcraft.plugins.circuit.routing.RouterVisualiser;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

@DisplayName("Digital Circuit")
@ShortName("circuit")
@CustomTools(CircuitToolsProvider.class)
public class VisualCircuit extends AbstractVisualModel {

    public VisualCircuit(Circuit model, VisualGroup root) {
        super(model, root);
    }

    public VisualCircuit(Circuit model) throws VisualModelInstantiationException {
        super(model);
        try {
            createDefaultFlatStructure();
        } catch (NodeCreationException e) {
            throw new VisualModelInstantiationException(e);
        }
    }

    @Override
    public Circuit getMathModel() {
        return (Circuit) super.getMathModel();
    }

    @Override
    public void validateConnection(Node first, Node second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Connections are only valid between different objects.");
        }

        if (second instanceof VisualConnection) {
            throw new InvalidConnectionException("Merging connections is not allowed.");
        }

        if (second instanceof VisualComponent) {
            for (Connection c: getConnections(second)) {
                if (c.getSecond() == second) {
                    throw new InvalidConnectionException("Only one connection is allowed as a driver.");
                }
            }
        }

        if (first instanceof VisualContact) {
            Contact contact = ((VisualContact) first).getReferencedContact();
            if (contact.isInput() && !contact.isPort()) {
                throw new InvalidConnectionException("Input pin of a component cannot be a driver.");
            }
            if (contact.isOutput() && contact.isPort()) {
                throw new InvalidConnectionException("Primary output cannot be a driver.");
            }
        }

        if (second instanceof VisualContact) {
            Contact contact = ((VisualContact) second).getReferencedContact();
            if (contact.isOutput() && !contact.isPort()) {
                throw new InvalidConnectionException("Output pin of a component cannot be driven.");
            }
            if (contact.isInput() && contact.isPort()) {
                throw new InvalidConnectionException("Primary input cannot be driven.");
            }
        }

        HashSet<Contact> drivenSet = new HashSet<>();
        Circuit circuit = getMathModel();
        Contact driver = null;
        if (first instanceof VisualConnection) {
            VisualConnection firstConnection = (VisualConnection) first;
            driver = CircuitUtils.findDriver(circuit, firstConnection.getReferencedConnection(), true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, firstConnection.getReferencedConnection(), true));
            }
        } else if (first instanceof VisualComponent) {
            VisualComponent firstComponent = (VisualComponent) first;
            driver = CircuitUtils.findDriver(circuit, firstComponent.getReferencedComponent(), true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, firstComponent.getReferencedComponent(), true));
            }
        }
        if (second instanceof VisualComponent) {
            VisualComponent secondComponent = (VisualComponent) second;
            drivenSet.addAll(CircuitUtils.findDriven(circuit, secondComponent.getReferencedComponent(), true));
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
        if (firstParent instanceof VisualFunctionComponent) {
            VisualFunctionComponent firstComponent = (VisualFunctionComponent) firstParent;
            Node secondParent = second.getParent();
            if (secondParent instanceof VisualFunctionComponent) {
                VisualFunctionComponent secondComponent = (VisualFunctionComponent) secondParent;
                if (firstComponent.getIsZeroDelay() && secondComponent.getIsZeroDelay()) {
                    throw new InvalidConnectionException("Zero delay components cannot be connected to each other.");
                }
            }
            if (second instanceof VisualContact) {
                VisualContact secondContact = (VisualContact) second;
                if (firstComponent.getIsZeroDelay() && secondContact.isPort() && secondContact.isOutput()) {
                    throw new InvalidConnectionException("Zero delay components cannot be connected to output ports.");
                }
            }
        }
    }

    @Override
    public VisualConnection connect(Node first, Node second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);
        if (first instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) first;
            Point2D splitPoint = connection.getSplitPoint();
            LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPoint);
            LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPoint);

            Container container = (Container) connection.getParent();
            VisualJoint joint = createJoint(container);
            joint.setPosition(splitPoint);
            remove(connection);

            VisualConnection predConnection = connect(connection.getFirst(), joint);
            predConnection.copyStyle(connection);
            ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);

            VisualConnection succConnection = connect(joint, connection.getSecond());
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            succConnection.copyStyle(connection);

            first = joint;
        }

        VisualCircuitConnection vConnection = null;
        if ((first instanceof VisualComponent) && (second instanceof VisualComponent)) {
            VisualComponent vComponent1 = (VisualComponent) first;
            VisualComponent vComponent2 = (VisualComponent) second;

            Node vParent = Hierarchy.getCommonParent(vComponent1, vComponent2);
            Container vContainer = (Container) Hierarchy.getNearestAncestor(vParent,
                    node -> (node instanceof VisualGroup) || (node instanceof VisualPage));
            if (mConnection == null) {
                MathNode mComponent1 = vComponent1.getReferencedComponent();
                MathNode mComponent2 = vComponent2.getReferencedComponent();
                mConnection = getMathModel().connect(mComponent1, mComponent2);
            }
            vConnection = new VisualCircuitConnection(mConnection, vComponent1, vComponent2);
            vConnection.setArrowLength(0.0);
            vContainer.add(vConnection);
        }
        return vConnection;
    }

    public Collection<VisualFunctionContact> getVisualFunctionContacts() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualFunctionContact.class);
    }

    public Collection<VisualFunctionComponent> getVisualFunctionComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualFunctionComponent.class);
    }

    public VisualFunctionContact getOrCreateContact(Container container, String name, IOType ioType) {
        // here "parent" is a container of a visual model
        if (name != null) {
            if (container == null) {
                container = getRoot();
            }
            for (Node n: container.getChildren()) {
                if (n instanceof VisualFunctionContact) {
                    VisualFunctionContact contact = (VisualFunctionContact) n;
                    String contactName = getMathModel().getName(contact.getReferencedContact());
                    if (name.equals(contactName)) {
                        return contact;
                    }
                } // TODO: if found something else with that name, return null or exception?
            }
        }

        Direction direction = Direction.WEST;
        if (ioType == null) {
            ioType = IOType.OUTPUT;
        }
        if (ioType == IOType.OUTPUT) {
            direction = Direction.EAST;
        }

        VisualFunctionContact vc = new VisualFunctionContact(new FunctionContact(ioType));
        vc.setDirection(direction);

        if (container instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) container;
            component.addContact(this, vc);
        } else {
            Container mathContainer = NamespaceHelper.getMathContainer(this, getRoot());
            mathContainer.add(vc.getReferencedComponent());
            add(vc);
        }
        if (name != null) {
            getMathModel().setName(vc.getReferencedComponent(), name);
        }
        vc.setPosition(new Point2D.Double(0.0, 0.0));
        return vc;
    }

    public VisualJoint createJoint(Container container) {
        if (container == null) {
            container = getRoot();
        }
        VisualJoint joint = new VisualJoint(new Joint());
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        mathContainer.add(joint.getReferencedComponent());
        container.add(joint);
        return joint;
    }

    public Collection<VisualContact> getVisualPorts() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualContact.class, contact -> contact.isPort());
    }

    public Collection<VisualContact> getVisualDrivers() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualContact.class, contact -> contact.isDriver());
    }

    public Collection<Environment> getEnvironmentNodes() {
        return Hierarchy.getChildrenOfType(getRoot(), Environment.class);
    }

    private WorkspaceEntry getWorkspaceEntry() {
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        for (WorkspaceEntry we: workspace.getWorks()) {
            ModelEntry me = we.getModelEntry();
            if (this == me.getVisualModel()) {
                return we;
            }
        }
        return null;
    }

    @NoAutoSerialisation
    public File getEnvironmentFile() {
        File file = null;
        for (Environment environment : getEnvironmentNodes()) {
            file = environment.getFile();
            File base = environment.getBase();
            if (base != null) {
                String basePath = base.getPath().replace("\\", "/");
                String filePath = file.getPath().replace("\\", "/");
                if (filePath.startsWith(basePath)) {
                    WorkspaceEntry we = getWorkspaceEntry();
                    File newBase = we == null ? null : we.getFile().getParentFile();
                    if (newBase != null) {
                        String relativePath = filePath.substring(basePath.length(), filePath.length());
                        while (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1, relativePath.length());
                        }
                        file = new File(newBase, relativePath);
                    }
                }
            }
            break;
        }
        return file;
    }

    @NoAutoSerialisation
    public void setEnvironmentFile(File file) {
        File oldFile = getEnvironmentFile();
        boolean envChanged = ((oldFile == null) && (file != null)) || ((oldFile != null) && !oldFile.equals(file));
        WorkspaceEntry we = getWorkspaceEntry();
        if (envChanged && (we != null)) {
            we.saveMemento();
            we.setChanged(true);
            File base = we.getFile().getParentFile();
            setEnvironment(file, base);
        }
    }

    public void updateEnvironmentFile() {
        File file = getEnvironmentFile();
        WorkspaceEntry we = getWorkspaceEntry();
        File base = (we == null) ? null : we.getFile().getParentFile();
        setEnvironment(file, base);
    }

    private void setEnvironment(File file, File base) {
        for (Environment environment : getEnvironmentNodes()) {
            remove(environment);
        }
        if (file != null) {
            Environment env = new Environment();
            env.setFile(file);
            env.setBase(base);
            add(env);
        }
    }

    @Override
    public void draw(Graphics2D g, Decorator decorator) {
        super.draw(g, decorator);
        if (CircuitLayoutSettings.getDebugRouting()) {
            RouterClient routerClient = new RouterClient();
            RouterTask routerTask = routerClient.registerObstacles(this);
            Router router = new Router();
            router.routeConnections(routerTask);
            RouterVisualiser.drawEverything(router, g);
        }
    }

    @Override
    public AbstractLayoutCommand getBestLayouter() {
        return new CircuitLayoutCommand();
    }

    @Override
    public void beforeSerialisation() {
        // Update environment file in case the base directory has changed, e.g. if the work is saved in a new location.
        updateEnvironmentFile();
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            properties.add(new EnvironmentFilePropertyDescriptor(this));
        } else if (node instanceof VisualFunctionContact) {
            VisualFunctionContact contact = (VisualFunctionContact) node;
            properties.add(new SetFunctionPropertyDescriptor(this, contact));
            properties.add(new ResetFunctionPropertyDescriptor(this, contact));
        } else if (node instanceof VisualCircuitComponent) {
            VisualCircuitComponent component = (VisualCircuitComponent) node;
            VisualContact mainOutput = component.getMainVisualOutput();
            if (mainOutput != null) {
                if (mainOutput instanceof VisualFunctionContact) {
                    VisualFunctionContact contact = (VisualFunctionContact) mainOutput;
                    properties.add(new SetFunctionPropertyDescriptor(this, contact));
                    properties.add(new ResetFunctionPropertyDescriptor(this, contact));
                }
                for (PropertyDescriptor property : mainOutput.getDescriptors()) {
                    String propertyName = property.getName();
                    if (Contact.PROPERTY_INIT_TO_ONE.equals(propertyName)
                            || Contact.PROPERTY_FORCED_INIT.equals(propertyName)) {
                        properties.add(property);
                    }
                }
            }
        }
        return properties;
    }

}
