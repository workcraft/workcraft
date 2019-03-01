package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class CircuitResetActiveHighCommand extends CircuitAbstractInitialisationCommand {

    @Override
    public String getDisplayName() {
        return "Insert active-high reset";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        String name = DialogUtils.showInput("Port name for active-high reset:", CircuitSettings.getResetName());
        if (name != null) {
            we.saveMemento();
            ResetUtils.insertReset(circuit, name, false);
        }
        return super.execute(we);
    }

}
