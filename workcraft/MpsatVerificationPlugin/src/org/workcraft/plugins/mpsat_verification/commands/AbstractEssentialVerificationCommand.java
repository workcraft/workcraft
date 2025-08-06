package org.workcraft.plugins.mpsat_verification.commands;

public abstract class AbstractEssentialVerificationCommand
        extends AbstractVerificationCommand {

    public static final Section SECTION
            = new Section("Individual essential properties [MPSat]", Position.TOP, 10);

    @Override
    public Section getSection() {
        return SECTION;
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

}
