package org.workcraft.plugins.son.tools;

import org.workcraft.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class TimeValueDisable implements Command {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, SON.class);
    }

    @Override
    public String getSection() {
        return "Time analysis";
    }

    @Override
    public String getDisplayName() {
        return "Enable/Disable time values";
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        SON net = (SON) me.getMathModel();
        SONSettings.setTimeVisibility(!SONSettings.getTimeVisibility());
        if (SONSettings.getTimeVisibility()) {
            TimeAlg.setProperties(net);
        } else {
            TimeAlg.removeProperties(net);
        }
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

}
