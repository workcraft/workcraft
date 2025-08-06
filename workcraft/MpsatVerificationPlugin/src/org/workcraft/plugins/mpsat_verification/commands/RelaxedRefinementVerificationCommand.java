package org.workcraft.plugins.mpsat_verification.commands;

public class RelaxedRefinementVerificationCommand
        extends RefinementVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Refinement (allow concurrency reduction)...";
    }

    @Override
    public boolean getAllowConcurrencyReduction() {
        return true;
    }

}
