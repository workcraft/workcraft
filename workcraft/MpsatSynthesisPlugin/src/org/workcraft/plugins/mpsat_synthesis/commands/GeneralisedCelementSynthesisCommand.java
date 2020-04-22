package org.workcraft.plugins.mpsat_synthesis.commands;

import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;

public class GeneralisedCelementSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public SynthesisMode getSynthesisMode() {
        return SynthesisMode.GENERALISED_CELEMENT_IMPLEMENTATION;
    }

    @Override
    public String getDisplayName() {
        return "Generalised C-element [MPSat]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

}
