package org.workcraft.plugins.petrify.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.utils.ExecutableUtils;

import java.util.Arrays;
import java.util.List;

public class TechnologyMappingSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        String gateLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if ((gateLibrary == null) || gateLibrary.isEmpty()) {
            return Arrays.asList("-tm");
        } else {
            return Arrays.asList("-tm", "-lib", gateLibrary);
        }
    }

    @Override
    public String getDisplayName() {
        return "Technology mapping [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public boolean technologyMapping() {
        return true;
    }

}
