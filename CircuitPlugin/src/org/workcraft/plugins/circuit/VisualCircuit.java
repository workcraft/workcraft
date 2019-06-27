package org.workcraft.plugins.circuit;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.FileReference;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.gui.properties.ModelProperties;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.tools.CommentGeneratorTool;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.circuit.commands.CircuitLayoutCommand;
import org.workcraft.plugins.circuit.commands.CircuitLayoutSettings;
import org.workcraft.plugins.circuit.routing.RouterClient;
import org.workcraft.plugins.circuit.routing.RouterVisualiser;
import org.workcraft.plugins.circuit.routing.impl.Router;
import org.workcraft.plugins.circuit.routing.impl.RouterTask;
import org.workcraft.plugins.circuit.tools.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.*;

@DisplayName("Digital Circuit")
@ShortName("circuit")
public class VisualCircuit extends AbstractVisualModel {

    public static final String PROPERTY_ENVIRONMENT = "Environment";

    public VisualCircuit(Circuit model) {
        this(model, null);
    }

    public VisualCircuit(Circuit model, VisualGroup root)  {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new CircuitSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new CircuitConnectionTool());
        tools.add(new FunctionComponentGeneratorTool());
        tools.add(new ContactGeneratorTool());
        tools.add(new CircuitSimulationTool());
        tools.add(new InitialisationAnalyserTool());
        tools.add(new CycleAnalyserTool());
        setGraphEditorTools(tools);
    }

    @Override
    public Circuit getMathModel() {
        return (Circuit) super.getMathModel();
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        if (first == second) {
            throw new InvalidConnectionException("Connections are only valid between different objects.");
        }

        if (second instanceof VisualConnection) {
            throw new InvalidConnectionException("Merging connections is not allowed.");
        }

        if ((second instanceof VisualContact) || (second instanceof VisualJoint)) {
            for (VisualConnection connection : getConnections(second)) {
                if (connection.getSecond() == second) {
                    throw new InvalidConnectionException("Only one connection is allowed as a driver.");
                }
            }
        }

        if (first instanceof VisualContact) {
            VisualContact secondContact = (VisualContact) first;
            if (secondContact.isInput() && !secondContact.isPort()) {
                throw new InvalidConnectionException("Input pin of a component cannot be a driver.");
            }
            if (secondContact.isOutput() && secondContact.isPort()) {
                throw new InvalidConnectionException("Primary output cannot be a driver.");
            }
        }

        if (second instanceof VisualContact) {
            VisualContact secondContact = (VisualContact) second;
            if (secondContact.isOutput() && !secondContact.isPort()) {
                throw new InvalidConnectionException("Output pin of a component cannot be driven.");
            }
            if (secondContact.isInput() && secondContact.isPort()) {
                throw new InvalidConnectionException("Primary input cannot be driven.");
            }
        }

        // Handle driver-driven relationship of input and output ports
        HashSet<Contact> drivenSet = new HashSet<>();
        Circuit circuit = getMathModel();
        Contact driver = null;
        if (first instanceof VisualConnection) {
            MathConnection firstConnection = ((VisualConnection) first).getReferencedConnection();
            driver = CircuitUtils.findDriver(circuit, firstConnection, true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, firstConnection, true));
            }
        } else if ((first instanceof VisualContact) || (first instanceof VisualJoint)) {
            MathNode firstComponent = ((VisualComponent) first).getReferencedComponent();
            driver = CircuitUtils.findDriver(circuit, firstComponent, true);
            if (driver != null) {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, driver, true));
            } else {
                drivenSet.addAll(CircuitUtils.findDriven(circuit, firstComponent, true));
            }
        }

        if ((second instanceof VisualContact) || (second instanceof VisualJoint)) {
            MathNode secondComponent = ((VisualComponent) second).getReferencedComponent();
            drivenSet.addAll(CircuitUtils.findDriven(circuit, secondComponent, true));
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

        // Handle zero delay components
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
    public VisualCircuitConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) throws InvalidConnectionException {
        validateConnection(first, second);

        if (first instanceof VisualConnection) {
            VisualConnection connection = (VisualConnection) first;
            Point2D splitPoint = connection.getSplitPoint();
            LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(connection, splitPoint);
            LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPoint);

            Container container = (Container) connection.getParent();
            VisualJoint joint = createJoint(container);
            joint.setPosition(TransformHelper.snapP5(splitPoint));
            remove(connection);

            VisualConnection predConnection = connect(connection.getFirst(), joint);
            predConnection.copyStyle(connection);
            ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);

            VisualConnection succConnection = connect(joint, connection.getSecond());
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            succConnection.copyStyle(connection);

            first = joint;
        }

        if (first instanceof VisualCircuitComponent) {
            first = ((VisualCircuitComponent) first).createContact(Contact.IOType.OUTPUT);
        }

        if (second instanceof VisualCircuitComponent) {
            second = ((VisualCircuitComponent) second).createContact(Contact.IOType.INPUT);
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

    public VisualFunctionContact getOrCreatePort(String name, Contact.IOType ioType) {
        VisualFunctionContact result = getVisualComponentByMathReference(name, VisualFunctionContact.class);
        if (result == null) {
            result = new VisualFunctionContact(new FunctionContact(ioType));
            result.setDefaultDirection();
            Container mathContainer = NamespaceHelper.getMathContainer(this, getRoot());
            mathContainer.add(result.getReferencedComponent());
            add(result);
            setMathName(result, name);
        }
        return result;
    }

    public VisualFunctionContact getOrCreateContact(VisualFunctionComponent component, String name, Contact.IOType ioType) {
        for (VisualFunctionContact contact : component.getVisualFunctionContacts()) {
            String contactName = getMathModel().getName(contact.getReferencedContact());
            if (name.equals(contactName)) {
                return contact;
            }
        }
        VisualFunctionContact result = new VisualFunctionContact(new FunctionContact(ioType));
        result.setDefaultDirection();
        component.addContact(result);
        setMathName(result, name);
        return result;
    }

    public VisualFunctionComponent createFunctionComponent(Container container) {
        if (container == null) {
            container = getRoot();
        }
        VisualFunctionComponent component = new VisualFunctionComponent(new FunctionComponent());
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        mathContainer.add(component.getReferencedComponent());
        container.add(component);
        return component;
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

    @Override
    public void afterDeserialisation() {
        super.afterDeserialisation();
        // FIXME: For backward compatibility convert Environment nodes to Environment property.
        Collection<Environment> environments = new ArrayList<>();
        environments.addAll(Hierarchy.getChildrenOfType(getRoot(), Environment.class));
        environments.addAll(Hierarchy.getChildrenOfType(getMathModel().getRoot(), Environment.class));
        for (Environment environment : environments) {
            Container container = (Container) environment.getParent();
            container.remove(environment);
            getMathModel().setEnvironmentFile(environment.getRelativePath());
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
    public ModelProperties getProperties(VisualNode node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            properties.add(getEnvironmentProperty());
        } else if (node instanceof VisualFunctionContact) {
            VisualFunctionContact contact = (VisualFunctionContact) node;
            properties.add(getSetFunctionProperty(contact));
            properties.add(getResetFunctionProperty(contact));
        } else if (node instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) node;
            VisualFunctionContact mainOutput = component.getMainVisualOutput();
            if (mainOutput != null) {
                properties.add(getSetFunctionProperty(mainOutput));
                properties.add(getResetFunctionProperty(mainOutput));
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

    private PropertyDescriptor getEnvironmentProperty() {
        return new PropertyDeclaration<Circuit, FileReference>(
                getMathModel(), PROPERTY_ENVIRONMENT, FileReference.class) {
            @Override
            public void setter(Circuit object, FileReference value) {
                object.setEnvironment(value);
            }
            @Override
            public FileReference getter(Circuit object) {
                return object.getEnvironment();
            }
        };
    }

    private PropertyDescriptor getSetFunctionProperty(VisualFunctionContact contact) {
        return new PropertyDeclaration<VisualFunctionContact, String>(
                contact, FunctionContact.PROPERTY_SET_FUNCTION, String.class, true, false) {
            @Override
            public void setter(VisualFunctionContact object, String value) {
                try {
                    BooleanFormula formula = CircuitUtils.parseContactFunction(VisualCircuit.this, object, value);
                    object.setSetFunction(formula);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public String getter(VisualFunctionContact object) {
                return StringGenerator.toString(object.getSetFunction());
            }
            @Override
            public boolean isVisible() {
                return contact.isDriver();
            }
        };
    }

    private PropertyDescriptor getResetFunctionProperty(VisualFunctionContact contact) {
        return new PropertyDeclaration<VisualFunctionContact, String>(
                contact, FunctionContact.PROPERTY_RESET_FUNCTION, String.class, true, false) {
            @Override
            public void setter(VisualFunctionContact object, String value) {
                try {
                    BooleanFormula formula = CircuitUtils.parseContactFunction(VisualCircuit.this, object, value);
                    object.setResetFunction(formula);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public String getter(VisualFunctionContact object) {
                return StringGenerator.toString(object.getResetFunction());
            }
            @Override
            public boolean isVisible() {
                return contact.isDriver();
            }
        };
    }

}
