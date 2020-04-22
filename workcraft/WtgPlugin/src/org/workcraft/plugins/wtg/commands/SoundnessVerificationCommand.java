package org.workcraft.plugins.wtg.commands;

import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.wtg.Wtg;
import org.workcraft.plugins.wtg.utils.VerificationUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class SoundnessVerificationCommand extends AbstractVerificationCommand implements ScriptableCommand<Boolean> {

    private static final String TITLE = "Verification result";

    @Override
    public String getDisplayName() {
        return "Soundness and consistency";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Wtg.class);
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
        final Wtg wtg = WorkspaceUtils.getAs(we, Wtg.class);
        boolean result = false;
        if (VerificationUtils.checkStructure(wtg)) {
            result = VerificationUtils.checkConsistency(wtg);
            if (result) {
                DialogUtils.showInfo("The model is sound and consistent.", TITLE);
            } else {
                DialogUtils.showWarning("Consistency is violated.", TITLE);
            }
        }
        return result;
    }

}
