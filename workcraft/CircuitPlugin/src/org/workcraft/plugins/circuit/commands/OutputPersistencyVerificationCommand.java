package org.workcraft.plugins.circuit.commands;

public class OutputPersistencyVerificationCommand
        extends CombinedVerificationCommand {

    @Override
    public int getPriority() {
        return 30;
    }

    @Override
    public String getDisplayName() {
        return "Output persistency (environment without dummies)";
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
    public boolean checkDeadlock() {
        return false;
    }

}
