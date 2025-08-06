package org.workcraft.plugins.circuit.commands;

public class ConformationVerificationCommand
        extends CombinedVerificationCommand {

    @Override
    public int getPriority() {
        return 40;
    }

    @Override
    public String getDisplayName() {
        return "Conformation";
    }

    @Override
    public Section getSection() {
        return SECTION;
    }

    @Override
    public boolean checkPersistency() {
        return false;
    }

    @Override
    public boolean checkDeadlock() {
        return false;
    }

}
