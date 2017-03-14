package org.workcraft.gui.graph.commands;

import org.workcraft.MenuOrdering;

public abstract class AbstractVerificationCommand implements Command, MenuOrdering {

    @Override
    public final String getSection() {
        return "!  Verification"; // 2 spaces - positions 3nd
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
