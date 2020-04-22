package org.workcraft.plugins.mpsat_synthesis.commands;

import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;

public class ComplexGateSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public SynthesisMode getSynthesisMode() {
        return SynthesisMode.COMPLEX_GATE_IMPLEMENTATION;
    }

    @Override
    public String getDisplayName() {
        return "Complex gate [MPSat]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

}
