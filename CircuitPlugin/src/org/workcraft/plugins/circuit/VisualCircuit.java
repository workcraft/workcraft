/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.circuit;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.Framework;
import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.circuit.tools.CircuitLayoutTool;
import org.workcraft.plugins.layout.AbstractLayoutTool;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

@DisplayName("Digital Circuit")
@ShortName("circuit")
@CustomTools(CircuitToolsProvider.class)
public class VisualCircuit extends AbstractVisualModel {

    private Circuit circuit;

    public VisualCircuit(Circuit model, VisualGroup root) {
        super(model, root);
        circuit = model;
    }

    public VisualCircuit(Circuit model) throws VisualModelInstantiationException {
        super(model);
        circuit = model;
        try {
            createDefaultFlatStructure();
        } catch (NodeCreationException e) {
            throw new VisualModelInstantiationException(e);
        }
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
            for (Connection c: this.getConnections(second)) {
                if (c.getSecond() == second) {
                    throw new InvalidConnectionException("Only one connection is allowed as a driver.");
                }
            }
        }

        if (first instanceof VisualContact) {
            Contact contact = ((VisualContact) first).getReferencedContact();
            if (contact.isInput() && !contact.isPort()) {
                throw new InvalidConnectionException("Inputs of components cannot be drivers.");
            }
        }

        if (second instanceof VisualContact) {
            Contact contact = ((VisualContact) second).getReferencedContact();
            if (contact.isOutput() && !contact.isPort()) {
                throw new InvalidConnectionException("Outputs of the components cannot be driven.");
            }
            if (contact.isInput() && contact.isPort()) {
                throw new InvalidConnectionException("Inputs from the environment cannot be driven.");
            }
        }

        HashSet<Contact> drivenSet = new HashSet<>();
        Circuit circuit = (Circuit) this.getMathModel();
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

            Container vContainer = (Container) connection.getParent();
            Container mParent = (Container) (connection.getReferencedConnection().getParent());
            Joint mJoint = new Joint();
            mParent.add(mJoint);
            VisualJoint vJoint = new VisualJoint(mJoint);
            vContainer.add(vJoint);
            vJoint.setPosition(splitPoint);
            remove(connection);

            VisualConnection predConnection = connect(connection.getFirst(), vJoint);
            predConnection.copyStyle(connection);
            ConnectionHelper.addControlPoints(predConnection, prefixLocationsInRootSpace);

            VisualConnection succConnection = connect(vJoint, connection.getSecond());
            ConnectionHelper.addControlPoints(succConnection, suffixLocationsInRootSpace);
            succConnection.copyStyle(connection);

            first = vJoint;
        }

        VisualCircuitConnection vConnection = null;
        if ((first instanceof VisualComponent) && (second instanceof VisualComponent)) {
            VisualComponent vComponent1 = (VisualComponent) first;
            VisualComponent vComponent2 = (VisualComponent) second;

            Node vParent = Hierarchy.getCommonParent(vComponent1, vComponent2);
            Container vContainer = (Container) Hierarchy.getNearestAncestor(vParent, new Func<Node, Boolean>() {
                @Override
                public Boolean eval(Node node) {
                    return (node instanceof VisualGroup) || (node instanceof VisualPage);
                }
            });
            if (mConnection == null) {
                MathNode mComponent1 = vComponent1.getReferencedComponent();
                MathNode mComponent2 = vComponent2.getReferencedComponent();
                mConnection = circuit.connect(mComponent1, mComponent2);
            }
            vConnection = new VisualCircuitConnection(mConnection, vComponent1, vComponent2);
            vConnection.setArrowLength(0.0);
            vContainer.add(vConnection);
        }
        return vConnection;
    }

    public String getMathName(VisualComponent component) {
        return getMathModel().getName(component.getReferencedComponent());
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
            circuit.setName(vc.getReferencedComponent(), name);
        }
        vc.setPosition(new Point2D.Double(0.0, 0.0));
        return vc;
    }

    public Collection<Environment> getEnvironments() {
        return Hierarchy.getChildrenOfType(getRoot(), Environment.class);
    }

    private WorkspaceEntry getWorkspaceEntry() {
        Framework framework = Framework.getInstance();
        GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
        return editor == null ? null : editor.getWorkspaceEntry();
    }

    @NoAutoSerialisation
    public File getEnvironmentFile() {
        File file = null;
        for (Environment env: getEnvironments()) {
            file = env.getFile();
            File base = env.getBase();
            if (base != null) {
                String basePath = base.getPath().replaceAll("\\\\", "/");
                String filePath = file.getPath().replaceAll("\\\\", "/");
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
    public void setEnvironmentFile(File value) {
        boolean envChanged = false;
        getWorkspaceEntry().captureMemento();

        for (Environment env: getEnvironments()) {
            remove(env);
            envChanged = true;
        }

        if (value != null) {
            Environment env = new Environment();
            env.setFile(value);
            File base = getWorkspaceEntry().getFile().getParentFile();
            env.setBase(base);
            add(env);
            envChanged = true;
        }

        if (envChanged) {
            getWorkspaceEntry().setChanged(true);
            getWorkspaceEntry().saveMemento();
        }
    }

    @Override
    public AbstractLayoutTool getBestLayoutTool() {
        return new CircuitLayoutTool();
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            properties.add(new EnvironmentFilePropertyDescriptor(this));
        } else if (node instanceof VisualFunctionContact) {
            VisualFunctionContact contact = (VisualFunctionContact) node;
            VisualContactFormulaProperties props = new VisualContactFormulaProperties(this);
            properties.add(props.getSetProperty(contact));
            properties.add(props.getResetProperty(contact));
        }
        return properties;
    }

}
