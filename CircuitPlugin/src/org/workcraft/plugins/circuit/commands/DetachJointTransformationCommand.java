package org.workcraft.plugins.circuit.commands;

import java.util.Collection;
import java.util.HashSet;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
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
        return Position.BOTTOM_MIDDLE;
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
            CircuitUtils.detachJoint(circuit, driver);
        }
    }

}
