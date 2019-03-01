package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.utils.InitialisationState;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

import java.util.HashSet;

public class CircuitResetVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Initialisation via forced primary inputs";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        InitialisationState initState = new InitialisationState(circuit);
        HashSet<String> incorrectlyInitialisedComponentRefs = new HashSet<>();
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver()) {
                if (contact.getForcedInit() || !initState.isCorrectlyInitialised(contact)) {
                    String ref = circuit.getNodeReference(contact);
                    incorrectlyInitialisedComponentRefs.add(ref);
                }
            }
        }
        if (incorrectlyInitialisedComponentRefs.isEmpty()) {
            DialogUtils.showInfo("The circuit is fully initialised via forced inputs");
            return true;
        } else {
            String msg = "The circuit cannot be initialised via forced inputs.\n" +
                    LogUtils.getTextWithRefs("Problematic signal", incorrectlyInitialisedComponentRefs);
            DialogUtils.showError(msg);
            return false;
        }
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we) && VerificationUtils.checkCircuitHasComponents(we);
    }

}
