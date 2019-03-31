package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class ResetActiveHighInsertionCommand extends AbstractInsertionCommand {

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
        String name = DialogUtils.showInput("Port name for active-high reset:", CircuitSettings.getResetName());
        if (name != null) {
            we.saveMemento();
            ResetUtils.insertReset(circuit, name, false);
        }
        return null;
    }

}
