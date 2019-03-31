package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.HashSet;

public class ForceInitTagSelfLoopsModificationCommand extends ForceInitAbstractModificationCommand {

    @Override
    public Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        HashSet<? extends Contact> changedContacts = ResetUtils.setForceInitSelfLoops(circuit, true);
        if (changedContacts.isEmpty()) {
            we.cancelMemento();
        } else {
            we.saveMemento();
            ArrayList<String> refs = ReferenceHelper.getReferenceList(circuit, changedContacts);
            LogUtils.logInfo(LogUtils.getTextWithRefs("Force init self-loop pin", refs));
        }
        return super.execute(we);
    }

}
