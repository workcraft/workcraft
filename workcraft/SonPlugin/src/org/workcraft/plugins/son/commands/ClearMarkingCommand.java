package org.workcraft.plugins.son.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ClearMarkingCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public Section getSection() {
        return new Section("Custom tools");
    }

    @Override
    public String getDisplayName() {
        return "Reset marking";
    }

    @Override
    public void run(WorkspaceEntry we) {
        SON net = WorkspaceUtils.getAs(we,  SON.class);
        net.clearMarking();
    }

}
