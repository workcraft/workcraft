package org.workcraft.plugins.circuit.tools;

public class CircuitDeadlockVerificationCommand extends CircuitVerificationCommand {
    @Override
    public Position getPosition() {
        return Position.TOP;
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
