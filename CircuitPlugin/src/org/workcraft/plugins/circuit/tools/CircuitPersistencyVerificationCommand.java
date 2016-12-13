package org.workcraft.plugins.circuit.tools;

public class CircuitPersistencyVerificationCommand extends CircuitVerificationCommand {
    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        return "Output persistency [MPSat]";
    }

    @Override
    public boolean checkConformation() {
        return false;
    }

    @Override
    public boolean checkDeadlock() {
        return false;
    }
}
