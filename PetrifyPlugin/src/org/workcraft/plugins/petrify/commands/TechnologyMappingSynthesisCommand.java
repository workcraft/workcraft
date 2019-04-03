package org.workcraft.plugins.petrify.commands;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.utils.ExecutableUtils;

public class TechnologyMappingSynthesisCommand extends AbstractPetrifySynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        String gateLibrary = ExecutableUtils.getAbsoluteCommandPath(CircuitSettings.getGateLibrary());
        if ((gateLibrary == null) || gateLibrary.isEmpty()) {
            return new String[] {"-tm"};
        } else {
            return new String[] {"-tm", "-lib", gateLibrary};
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

    public boolean technologyMapping() {
        return true;
    }

}
