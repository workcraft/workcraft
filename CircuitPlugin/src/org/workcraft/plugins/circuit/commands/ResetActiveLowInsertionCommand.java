package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ResetActiveLowInsertionCommand extends AbstractInsertionCommand {

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
