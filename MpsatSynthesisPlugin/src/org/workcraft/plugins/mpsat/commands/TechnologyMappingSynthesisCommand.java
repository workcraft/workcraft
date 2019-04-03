package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.SynthesisMode;

public class TechnologyMappingSynthesisCommand extends AbstractPetrifySynthesisCommand {

    @Override
    public SynthesisMode getSynthesisMode() {
        return SynthesisMode.TECH_MAPPING;
    }

    @Override
    public String getDisplayName() {
        return "Technology mapping [MPSat]";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

}
