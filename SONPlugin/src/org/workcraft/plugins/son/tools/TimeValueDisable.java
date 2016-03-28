package org.workcraft.plugins.son.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeValueDisable implements Tool {

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, SON.class);
    }

    public String getSection() {
        return "Time analysis";
    }

    public String getDisplayName() {
        return "Enable/Disable time values";
    }

    public void run(WorkspaceEntry we) {
        SON net = (SON) we.getModelEntry().getMathModel();
        SONSettings.setTimeVisibility(!SONSettings.getTimeVisibility());
        if (SONSettings.getTimeVisibility()) {
        	TimeAlg.setProperties(net);
        } else {
        	TimeAlg.removeProperties(net);
        }
    }

}
