package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

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
    public boolean isApplicableTo(VisualNode node) {
        if (node instanceof VisualContact) {
            VisualContact contact = (VisualContact) node;
            return contact.isDriver();
        }
        return false;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
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
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> drivers = new HashSet<>();
        if (model instanceof VisualCircuit) {
            VisualCircuit circuit = (VisualCircuit) model;
            for (VisualContact driver: circuit.getVisualDrivers()) {
                if (circuit.getConnections(driver).size() > 1) {
                    drivers.add(driver);
                }
            }
            Collection<VisualNode> selection = circuit.getSelection();
            if (!selection.isEmpty()) {
                HashSet<VisualNode> selectedDrivers = new HashSet<>(selection);
                for (VisualNode node: selection) {
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
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualContact)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualContact driver = (VisualContact) node;
            VisualJoint joint = CircuitUtils.detachJoint(circuit, driver);
            if (joint != null) {
                model.addToSelection(joint);
            }
        }
    }

}
