package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.util.HashSet;

public class TagForceInitSequentialGatesCommand extends CircuitAbstractInitialisationCommand {

    @Override
    public String getDisplayName() {
        return "Tag force init for all sequential gates";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        HashSet<? extends Contact> changedContacts = ResetUtils.setForceInitSequentialGates(circuit, true);
        if (changedContacts.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
        }
        return null;
    }

}
