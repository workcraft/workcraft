package org.workcraft.plugins.petrify.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.util.ToolUtils;

public class PetrifyStandardCelementSynthesisCommand extends PetrifyAbstractSynthesisCommand {

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
        return "Standard C-element [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public boolean boxSequentialComponents() {
        return false;
    }

    @Override
    public boolean boxCombinationalComponents() {
        return false;
    }

    @Override
    public boolean sequentialAssign() {
        return true;
    }

}
