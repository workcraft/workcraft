package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.SynthesisMode;

public class GeneralisedCelementSynthesisCommand extends AbstractPetrifySynthesisCommand {

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
