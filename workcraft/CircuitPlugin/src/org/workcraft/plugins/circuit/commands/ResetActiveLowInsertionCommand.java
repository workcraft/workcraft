package org.workcraft.plugins.circuit.commands;

public class ResetActiveLowInsertionCommand extends AbstractResetInsertionCommand {

    @Override
    public boolean isActiveLow() {
        return true;
    }

}
