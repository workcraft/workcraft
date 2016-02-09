package org.workcraft;

public abstract class SynthesisTool extends PromotedTool implements MenuOrdering {

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
