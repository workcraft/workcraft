package org.workcraft.plugins.son.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ErrTracingResetCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public String getSection() {
        return "Error tracing";
    }

    @Override
    public String getDisplayName() {
        return "Reset fault/error states";
    }

    @Override
    public void run(WorkspaceEntry we) {
        SON net = WorkspaceUtils.getAs(we, SON.class);
        net.resetErrStates();
    }

}
