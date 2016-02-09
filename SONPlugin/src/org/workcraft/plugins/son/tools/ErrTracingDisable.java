package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ErrTracingDisable implements Tool{

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, SON.class);
    }

    public String getSection(){
        return "Error tracing";
    }

    public String getDisplayName(){
        return "Enable/Disable error tracing";
    }

    public void run(WorkspaceEntry we){
        SONSettings.setErrorTracing(!SONSettings.isErrorTracing());
    }

}
