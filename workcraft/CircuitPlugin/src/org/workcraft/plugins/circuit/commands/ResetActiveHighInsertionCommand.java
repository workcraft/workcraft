package org.workcraft.plugins.circuit.commands;

public class ResetActiveHighInsertionCommand extends AbstractResetInsertionCommand {

    @Override
    public boolean isActiveLow() {
        return false;
    }

}
