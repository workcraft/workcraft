package org.workcraft.plugins.mpsat.commands;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class MpsatComplexGateSynthesisCommand extends MpsatAbstractSynthesisCommand {

    @Override
    public MpsatSynthesisMode getSynthesisMode() {
        return MpsatSynthesisMode.COMPLEX_GATE_IMPLEMENTATION;
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
