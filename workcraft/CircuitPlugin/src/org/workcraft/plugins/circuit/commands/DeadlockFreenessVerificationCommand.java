package org.workcraft.plugins.circuit.commands;

public class DeadlockFreenessVerificationCommand
        extends CombinedVerificationCommand {

    @Override
    public int getPriority() {
        return 20;
    }

    @Override
    public String getDisplayName() {
        return "Deadlock freeness (no determinisation)";
    }

    @Override
    public Section getSection() {
        return SECTION;
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
