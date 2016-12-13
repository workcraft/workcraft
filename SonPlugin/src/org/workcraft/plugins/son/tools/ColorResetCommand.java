package org.workcraft.plugins.son.tools;

import org.workcraft.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ColorResetCommand implements Command {

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, SON.class);
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
    public ModelEntry run(ModelEntry me) {
        SON net = (SON) me.getMathModel();
        net.refreshAllColor();
        return me;
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        run(we.getModelEntry());
        return we;
    }

}
