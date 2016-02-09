package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ColorResetTool implements Tool {

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, SON.class);
    }

    public String getSection(){
        return "Custom tools";
    }

    public String getDisplayName(){
        return "Reset color to default";
    }

    public void run(WorkspaceEntry we){
        SON net=(SON)we.getModelEntry().getMathModel();
        net.refreshAllColor();
    }

}
