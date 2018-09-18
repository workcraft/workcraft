package org.workcraft.plugins.circuit.commands;

import org.workcraft.NodeTransformer;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.circuit.utils.GateUtils;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
    public boolean isApplicableTo(Node node) {
        return node instanceof VisualCircuitConnection;
    }

    @Override
    public boolean isEnabled(ModelEntry me, Node node) {
        return true;
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

    @Override
    public Collection<Node> collect(Model model) {
        Collection<Node> result = new HashSet<>();
        if (model instanceof VisualModel) {
            VisualModel visualModel = (VisualModel) model;
            result.addAll(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualCircuitConnection.class));
            result.retainAll(visualModel.getSelection());
        }
        return result;
    }

    @Override
    public void transform(Model model, Node node) {
        if ((model instanceof VisualCircuit) && (node instanceof VisualCircuitConnection)) {
            VisualCircuit circuit = (VisualCircuit) model;
            VisualCircuitConnection connection = (VisualCircuitConnection) node;
            VisualFunctionComponent buffer = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateWithin(circuit, buffer, connection);
            GateUtils.propagateInitialState(circuit, buffer);
        }
    }

}
