package org.workcraft.plugins.son.commands;

import org.workcraft.gui.graph.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class ErrTracingDisableCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public String getSection() {
        return "Error tracing";
    }

    @Override
    public String getDisplayName() {
        return "Enable/Disable error tracing";
    }

    @Override
    public void run(WorkspaceEntry we) {
        SONSettings.setErrorTracing(!SONSettings.isErrorTracing());
    }

}
