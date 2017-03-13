package org.workcraft.plugins.circuit.commands;

public class CircuitPersistencyVerificationCommand extends CircuitVerificationCommand {
    @Override
    public Position getPosition() {
        return null;
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
