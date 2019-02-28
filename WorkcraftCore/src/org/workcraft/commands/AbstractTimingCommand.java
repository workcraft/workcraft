package org.workcraft.commands;

public abstract class AbstractTimingCommand implements ScriptableCommand<Boolean>, MenuOrdering {

    @Override
    public final String getSection() {
        return "!Timing"; // 0 spaces - positions 5th
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
