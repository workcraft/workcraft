package org.workcraft.plugins.circuit.tools;

public class CheckCircuitDeadlockTool extends CheckCircuitTool {
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
    public boolean checkHazard() {
        return false;
    }
}
