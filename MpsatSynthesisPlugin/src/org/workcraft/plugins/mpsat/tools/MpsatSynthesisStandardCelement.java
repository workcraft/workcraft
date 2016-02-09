package org.workcraft.plugins.mpsat.tools;

import org.workcraft.plugins.mpsat.MpsatSynthesisMode;

public class MpsatSynthesisStandardCelement extends MpsatSynthesis {

    @Override
    public MpsatSynthesisMode getSynthesisMode() {
        return MpsatSynthesisMode.STANDARD_CELEMENT_IMPLEMENTATION;
    }

    @Override
    public String getDisplayName() {
        return "Standard C-element [MPSat]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

}
