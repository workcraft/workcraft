package org.workcraft.plugins.circuit.tools;

public class CircuitConformationChecker extends CircuitChecker {
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
