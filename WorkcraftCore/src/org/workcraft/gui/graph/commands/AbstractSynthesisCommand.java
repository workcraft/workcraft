package org.workcraft.gui.graph.commands;

import org.workcraft.MenuOrdering;

public abstract class AbstractSynthesisCommand implements Command, MenuOrdering {

    @Override
    public final String getSection() {
        return "! Synthesis";  // 1 space - positions 4th
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

}
