package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class TechnologyMappingSynthesisCommand extends AbstractMpsatSynthesisCommand {

    @Override
    public MpsatSynthesisMode getSynthesisMode() {
        return MpsatSynthesisMode.TECH_MAPPING;
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
