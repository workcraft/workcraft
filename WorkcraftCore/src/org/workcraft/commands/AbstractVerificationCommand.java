package org.workcraft.commands;

public abstract class AbstractVerificationCommand implements ScriptableCommand<Boolean>, MenuOrdering {

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
