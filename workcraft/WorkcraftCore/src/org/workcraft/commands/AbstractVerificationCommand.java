package org.workcraft.commands;

import org.workcraft.utils.CommandUtils;

public abstract class AbstractVerificationCommand implements ScriptableCommand<Boolean>, MenuOrdering {

    public static final String SECTION_TITLE = CommandUtils.makePromotedSectionTitle("Verification", 3);

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
