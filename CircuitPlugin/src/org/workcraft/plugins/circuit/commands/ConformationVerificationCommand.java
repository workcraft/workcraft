package org.workcraft.plugins.circuit.commands;

public class ConformationVerificationCommand extends CombinedVerificationCommand {

    @Override
    public int getPriority() {
        return 1;
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
