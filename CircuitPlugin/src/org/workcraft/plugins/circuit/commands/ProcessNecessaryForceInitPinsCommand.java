package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class ProcessNecessaryForceInitPinsCommand extends CircuitAbstractInitialisationCommand {

    @Override
    public String getDisplayName() {
        return "Add force init to pins if necessary to complete initialisation";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        HashSet<? extends Contact> changedContacts = ResetUtils.tagNecessaryForceInitPins(circuit);
        if (changedContacts.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
            ArrayList<String> refs = ReferenceHelper.getReferenceList(circuit, changedContacts);
            LogUtils.logInfo(LogUtils.getTextWithRefs("Force init pin", refs));
        }
        return super.execute(we);
    }

}
