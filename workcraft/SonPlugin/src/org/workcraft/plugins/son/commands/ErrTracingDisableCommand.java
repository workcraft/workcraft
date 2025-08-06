package org.workcraft.plugins.son.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ErrTracingDisableCommand implements Command {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, SON.class);
    }

    @Override
    public Section getSection() {
        return new Section("Error tracing");
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
