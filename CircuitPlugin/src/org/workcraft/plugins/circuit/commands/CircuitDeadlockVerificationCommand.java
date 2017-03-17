package org.workcraft.plugins.circuit.commands;

public class CircuitDeadlockVerificationCommand extends CircuitVerificationCommand {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String getDisplayName() {
        return "Deadlock [MPSat]";
    }

    @Override
    public boolean checkConformation() {
        return false;
    }

    @Override
    public boolean checkPersistency() {
        return false;
    }

}
