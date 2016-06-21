package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ErrTracingReset implements Tool {

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    public String getSection() {
        return "Error tracing";
    }

    public String getDisplayName() {
        return "Reset fault/error states";
    }

    public void run(WorkspaceEntry we) {
        SON net = (SON) we.getModelEntry().getMathModel();
        net.resetErrStates();
    }

}
