package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.GateUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.Collection;
import java.util.HashSet;

public class InsertBufferTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Insert buffers into selected wires";
    }

    @Override
    public String getPopupName() {
        return "Insert buffer";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualCircuitConnection;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> result = new HashSet<>();
        result.addAll(Hierarchy.getDescendantsOfType(model.getRoot(), VisualCircuitConnection.class));
        result.retainAll(model.getSelection());
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualCircuitConnection)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualCircuitConnection connection = (VisualCircuitConnection) node;
            VisualFunctionComponent buffer = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateWithin(circuit, buffer, connection);
            GateUtils.propagateInitialState(circuit, buffer);
        }
    }

}
