package org.workcraft.commands;

import org.workcraft.utils.CommandUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractSynthesisCommand implements ScriptableCommand<WorkspaceEntry>, MenuOrdering {

    private static final String SECTION_TITLE = CommandUtils.makePromotedSectionTitle("Synthesis", 4);

    @Override
    public final String getSection() {
        return SECTION_TITLE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return null;
    }

}
