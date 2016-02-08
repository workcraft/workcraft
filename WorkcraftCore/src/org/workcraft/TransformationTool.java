package org.workcraft;

public abstract class TransformationTool extends PromotedTool implements MenuOrdering {

    @Override
    public String getSection() {
        return "!   Transformations";  // 3 spaces - positions 2nd
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
