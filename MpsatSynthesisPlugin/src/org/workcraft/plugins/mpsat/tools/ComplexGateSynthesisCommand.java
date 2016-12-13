package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class ComplexGateSynthesisCommand extends AbstractMpsatSynthesisCommand {

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
        return null;
    }

}
