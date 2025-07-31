package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractPGMinerCommand implements Command {

    public static final Category CATEGORY = new Category("Process Mining", 4);

    @Override
    public Category getCategory() {
        return CATEGORY;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }

}
