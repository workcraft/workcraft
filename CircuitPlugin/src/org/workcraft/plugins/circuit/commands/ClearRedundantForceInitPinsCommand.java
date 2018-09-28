package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.HashSet;

public class ClearRedundantForceInitPinsCommand extends CircuitAbstractInitialisationCommand {

    @Override
    public String getDisplayName() {
        return "Clear redundant force init from pins";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        HashSet<? extends Contact> changedContacts = ResetUtils.clearRedundantForceInitPins(circuit);
        if (changedContacts.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
        }
        return null;
    }

}
