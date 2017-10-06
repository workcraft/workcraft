package org.workcraft.plugins.circuit.commands;

public class CircuitOutputPersistencyVerificationCommand extends CircuitVerificationCommand {

    @Override
    public int getPriority() {
        return 1;
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
