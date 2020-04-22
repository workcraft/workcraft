package org.workcraft.plugins.mpsat_synthesis.commands;

import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;

public class StandardCelementSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public SynthesisMode getSynthesisMode() {
        return SynthesisMode.STANDARD_CELEMENT_IMPLEMENTATION;
    }

    @Override
    public String getDisplayName() {
        return "Standard C-element [MPSat]";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

}
