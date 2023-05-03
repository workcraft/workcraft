package org.workcraft.plugins.mpsat_synthesis.commands;

import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;

public class TechnologyMappingSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public SynthesisMode getSynthesisMode() {
        return SynthesisMode.TECHNOLOGY_MAPPING;
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
