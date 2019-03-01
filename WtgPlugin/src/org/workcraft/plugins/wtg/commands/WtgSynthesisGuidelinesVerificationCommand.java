package org.workcraft.plugins.wtg.commands;

import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.plugins.wtg.utils.VerificationUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class WtgSynthesisGuidelinesVerificationCommand extends AbstractVerificationCommand {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Synthesis guidelines";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Wtg.class);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        final Wtg wtg = WorkspaceUtils.getAs(we, Wtg.class);
        boolean result = VerificationUtils.synthesisGuidelines(wtg);
        if (result) {
            DialogUtils.showInfo("The model follows the synthesis guidelines.", TITLE);
        }
        return result;
    }

}
