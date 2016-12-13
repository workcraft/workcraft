package org.workcraft;

public abstract class AbstractVerificationCommand extends AbstractPromotedCommand implements MenuOrdering {

    @Override
    public String getSection() {
        return "!  Verification"; // 2 spaces - positions 3nd
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
