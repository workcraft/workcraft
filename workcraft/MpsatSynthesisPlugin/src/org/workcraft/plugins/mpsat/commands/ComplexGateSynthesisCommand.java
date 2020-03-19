package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.SynthesisMode;

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
