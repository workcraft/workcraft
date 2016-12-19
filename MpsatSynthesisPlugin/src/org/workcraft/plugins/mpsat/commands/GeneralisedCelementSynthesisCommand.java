package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class GeneralisedCelementSynthesisCommand extends AbstractMpsatSynthesisCommand {

    @Override
    public MpsatSynthesisMode getSynthesisMode() {
        return MpsatSynthesisMode.GENERALISED_CELEMENT_IMPLEMENTATION;
    }

    @Override
    public String getDisplayName() {
        return "Generalised C-element [MPSat]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

}
