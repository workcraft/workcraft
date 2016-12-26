package org.workcraft.plugins.circuit.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.NodeTransformer;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.commands.AbstractTransformationCommand;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class DetachJointTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Detach joints (selected or all)";
    }

    @Override
    public String getPopupName() {
        return "Detach joint";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(Node node) {
        if (node instanceof VisualContact) {
            VisualContact contact = (VisualContact) node;
            return contact.isDriver();
        }
        return false;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        if (node instanceof VisualContact) {
            VisualModel visualModel = me.getVisualModel();
            return visualModel.getConnections(node).size() > 1;
        }
        return false;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> drivers = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            for (VisualContact driver: circuit.getVisualDrivers()) {
                if (circuit.getConnections(driver).size() > 1) {
                    drivers.add(driver);
                }
            }
            Collection<Node> selection = circuit.getSelection();
            if (!selection.isEmpty()) {
                HashSet<Node> selectedDrivers = new HashSet<>(selection);
                for (Node node: selection) {
                    if (node instanceof VisualCircuitComponent) {
                        VisualCircuitComponent component = (VisualCircuitComponent) node;
                        selectedDrivers.addAll(component.getVisualOutputs());
                    }
                }
                selectedDrivers.retainAll(drivers);
                if (!selectedDrivers.isEmpty()) {
                    drivers.retainAll(selectedDrivers);
                }
            }
        }
        return drivers;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualContact)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualContact driver = (VisualContact) node;
            if (driver.isDriver() && (circuit.getConnections(driver).size() > 1)) {
                Set<Connection> connections = new HashSet<>(model.getConnections(node));

                Container container = (Container) driver.getParent();
                if (container instanceof VisualCircuitComponent) {
                    container = (Container) container.getParent();
                }
                VisualJoint joint = circuit.createJoint(container);
                joint.setRootSpacePosition(driver.getRootSpacePosition());

                try {
                    circuit.connect(driver, joint);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarningLine(e.getMessage());
                }

                for (Connection connection: connections) {
                    if (!(connection instanceof VisualCircuitConnection)) continue;
                    circuit.remove(connection);
                    try {
                        Node driven = connection.getSecond();
                        VisualCircuitConnection newConnection = (VisualCircuitConnection) circuit.connect(joint, driven);
                        newConnection.copyShape((VisualCircuitConnection) connection);
                        newConnection.copyStyle((VisualCircuitConnection) connection);
                    } catch (InvalidConnectionException e) {
                        LogUtils.logWarningLine(e.getMessage());
                    }
                }
            }
        }
    }

}
