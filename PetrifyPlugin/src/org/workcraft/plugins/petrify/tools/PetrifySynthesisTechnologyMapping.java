package org.workcraft.plugins.petrify.tools;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.util.ToolUtils;

public class PetrifySynthesisTechnologyMapping extends PetrifySynthesis {

    @Override
    public String[] getSynthesisParameter() {
        String[] result;
        String gateLibrary = ToolUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if ((gateLibrary == null) || gateLibrary.isEmpty()) {
            result = new String[1];
            result[0] = "-tm";
        } else {
            result = new String[3];
            result[0] = "-tm";
            result[1] = "-lib";
            result[2] = gateLibrary;
        }
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Technology mapping [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

}
