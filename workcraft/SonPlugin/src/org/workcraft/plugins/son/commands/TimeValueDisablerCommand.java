package org.workcraft.plugins.son.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.algorithm.TimeAlg;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeValueDisablerCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public Section getSection() {
        return new Section("Time analysis");
    }

    @Override
    public String getDisplayName() {
        return "Enable/Disable time values";
    }

    @Override
    public void run(WorkspaceEntry we) {
        SON net = WorkspaceUtils.getAs(we, SON.class);
        SONSettings.setTimeVisibility(!SONSettings.getTimeVisibility());
        if (SONSettings.getTimeVisibility()) {
            TimeAlg.setProperties(net);
        } else {
            TimeAlg.removeProperties(net);
        }
    }

}
