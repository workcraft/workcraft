package org.workcraft.plugins.son.tools;

import org.workcraft.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ColorResetCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public String getSection() {
        return "Custom tools";
    }

    @Override
    public String getDisplayName() {
        return "Reset color to default";
    }

    @Override
    public void run(WorkspaceEntry we) {
        SON net = WorkspaceUtils.getAs(we, SON.class);
        net.refreshAllColor();
    }

}
