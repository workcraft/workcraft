package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.tools.InitialisationAnalyserTool;
import org.workcraft.plugins.circuit.utils.ResetUtils;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.Set;

public class ResetVerificationCommand extends AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

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
    public void run(WorkspaceEntry we) {
        execute(we);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return null;
        }
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        Set<Contact> problematicContacts = ResetUtils.getInitialisationProblemContacts(circuit);
        for (FunctionContact contact : circuit.getFunctionContacts()) {
            if (contact.isPin() && contact.isDriver() && contact.getForcedInit()) {
                problematicContacts.add(contact);
            }
        }
        if (problematicContacts.isEmpty()) {
            DialogUtils.showInfo("The circuit is fully initialised via forced inputs");
            return true;
        } else {
            final Framework framework = Framework.getInstance();
            if (framework.isInGuiMode()) {
                final Toolbox toolbox = framework.getMainWindow().getCurrentToolbox();
                toolbox.selectTool(toolbox.getToolInstance(InitialisationAnalyserTool.class));
            }
            Collection<String> refs = ReferenceHelper.getReferenceList(circuit, problematicContacts);
            String msg = "The circuit cannot be initialised via forced inputs.\n" +
                    TextUtils.wrapMessageWithItems("Problematic signal", refs);

            DialogUtils.showError(msg);
            return false;
        }
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we) && VerificationUtils.checkCircuitHasComponents(we);
    }

}
