package org.workcraft.plugins.petrify.commands;

import java.util.Arrays;
import java.util.List;

public class ComplexGateSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Arrays.asList("-cg");
    }

    @Override
    public String getDisplayName() {
        return "Complex gate [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

}
