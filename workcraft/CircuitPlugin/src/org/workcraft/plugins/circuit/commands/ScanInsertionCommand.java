package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ScanInsertionCommand extends AbstractInsertionCommand {

    @Override
    public String getDisplayName() {
        return "Insert scan for path breaker components";
    }

    @Override
    public void insert(WorkspaceEntry we) {
        ScanUtils.insertScan(we);
    }

}
