package org.workcraft.gui.graph.commands;

import org.workcraft.MenuOrdering;
import org.workcraft.MenuOrdering.Position;

public abstract class AbstractSynthesisCommand extends AbstractPromotedCommand implements MenuOrdering {

    @Override
    public String getSection() {
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
