package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.Command;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractAlgebraCommand implements Command {

    public static final Category CATEGORY = new Category("Algebra", 3);

    @Override
    public Category getCategory() {
        return CATEGORY;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCpog.class);
    }
}
