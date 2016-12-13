package org.workcraft.plugins.circuit.tools;

public class CircuitConformationVerificationCommand extends CircuitVerificationCommand {
    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        return "Conformation [MPSat]";
    }

    @Override
    public boolean checkDeadlock() {
        return false;
    }

    @Override
    public boolean checkPersistency() {
        return false;
    }
}
