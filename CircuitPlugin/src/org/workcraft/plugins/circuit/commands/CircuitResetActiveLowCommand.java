package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CircuitResetActiveLowCommand extends CircuitAbstractInitialisationCommand {

    @Override
    public String getDisplayName() {
        return "Insert active-low reset";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        String name = DialogUtils.showInput("Port name for active-low reset:", CircuitSettings.getResetName());
        if (name != null) {
            we.saveMemento();
            ResetUtils.insertReset(circuit, name, true);
        }
        return null;
    }

}
