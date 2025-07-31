package org.workcraft.plugins.mpsat_verification.commands;

public abstract class AbstractRefinementVerificationCommand
        extends AbstractVerificationCommand {

    public static final Section SECTION
            = new Section("Refinement-like properties [MPSat]", Position.MIDDLE, 0);

    @Override
    public Section getSection() {
        return SECTION;
    }

}
