package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualJoint;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.SelectionUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class DetachJointTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Detach joints (selected or all)";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Detach joint";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        if (node instanceof VisualContact contact) {
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
    public Collection<VisualContact> collectNodes(VisualModel model) {
        Collection<VisualContact> result = new HashSet<>();
        if (model instanceof VisualCircuit circuit) {
            for (VisualContact driver: circuit.getVisualDrivers()) {
                if (circuit.getConnections(driver).size() > 1) {
                    result.add(driver);
                }
            }
            SelectionUtils.retainSelectedContacts(circuit, result);
        }
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit circuit) && (node instanceof VisualContact driver)) {
            VisualJoint joint = CircuitUtils.detachJoint(circuit, driver);
            if (joint != null) {
                model.addToSelection(joint);
            }
        }
    }

}
