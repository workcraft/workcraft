package org.workcraft.plugins.son.tools;

import org.workcraft.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ErrTracingResetCommand implements Command {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, SON.class);
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
    public ModelEntry run(ModelEntry me) {
        SON net = (SON) me.getMathModel();
        net.resetErrStates();
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

}
