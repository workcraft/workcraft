package org.workcraft.plugins.circuit.tools;

public class CheckCircuitHazardTool extends CheckCircuitTool {
    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        return "Hazard [MPSat]";
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
