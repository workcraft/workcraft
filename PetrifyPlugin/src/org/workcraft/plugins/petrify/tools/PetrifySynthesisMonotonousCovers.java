package org.workcraft.plugins.petrify.tools;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.util.ToolUtils;

public class PetrifySynthesisMonotonousCovers extends PetrifySynthesis {

    @Override
    public String[] getSynthesisParameter() {
        String[] result = new String[1];
        String gateLibrary = ToolUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if ((gateLibrary == null) || gateLibrary.isEmpty()) {
            result = new String[1];
            result[0] = "-mc";
        } else {
            result = new String[3];
            result[0] = "-mc";
            result[1] = "-lib";
            result[2] = gateLibrary;
        }
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Monotonous covers [Petrify with -mc option] (experimantal)";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public boolean boxSequentialComponents() {
        return true;
    }

    @Override
    public boolean boxCombinationalComponents() {
        return true;
    }

}
