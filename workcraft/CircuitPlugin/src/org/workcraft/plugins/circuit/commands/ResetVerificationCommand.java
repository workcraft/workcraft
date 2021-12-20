package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.Toolbox;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
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

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Initialisation via forced input ports";
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
        if (!isApplicableTo(we)) {
            return null;
        }
        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (!VerificationUtils.checkBlackboxComponents(circuit)) {
            return null;
        }

        Set<Contact> problematicPins = ResetUtils.getInitialisationProblemPins(circuit);
        if (problematicPins.isEmpty()) {
            DialogUtils.showInfo("The circuit is fully initialised via the currently forced input ports.", TITLE);
            return true;
        }

        Framework framework = Framework.getInstance();
        if (framework.isInGuiMode()) {
            Toolbox toolbox = framework.getMainWindow().getCurrentToolbox();
            toolbox.selectTool(toolbox.getToolInstance(InitialisationAnalyserTool.class));
        }

        Collection<String> refs = ReferenceHelper.getReferenceList(circuit, problematicPins);
        String msg = "The currently forced input ports are insufficient to fully initialised the circuit.\n" +
                TextUtils.wrapMessageWithItems("Problematic signal", refs);

        DialogUtils.showError(msg, TITLE);
        return false;
    }

}
