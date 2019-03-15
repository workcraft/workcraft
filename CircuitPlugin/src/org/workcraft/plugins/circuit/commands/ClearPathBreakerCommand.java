package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.utils.CycleUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;
import java.util.Set;

public class ClearPathBreakerCommand extends CircuitAbstractPathbreakerCommand {

    @Override
    public String getDisplayName() {
        return "Clear path breaker from all pins and components";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        we.captureMemento();
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Set<? extends Contact> changedContacts = CycleUtils.clearPathBreakerContacts(circuit);
        Set<? extends FunctionComponent> changedComponents = CycleUtils.clearPathBreakerComponents(circuit);
        if (changedContacts.isEmpty() && changedComponents.isEmpty()) {
            we.uncaptureMemento();
        } else {
            we.saveMemento();
            ArrayList<String> refs = ReferenceHelper.getReferenceList(circuit, changedContacts);
            LogUtils.logInfo(LogUtils.getTextWithRefs("Path breaker is cleared for contact", refs));
            refs = ReferenceHelper.getReferenceList(circuit, changedContacts);
            LogUtils.logInfo(LogUtils.getTextWithRefs("Path breaker is cleared for component", refs));
        }
        return super.execute(we);
    }

}
