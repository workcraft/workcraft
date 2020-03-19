package org.workcraft.plugins.atacs.commands;

import java.util.Arrays;
import java.util.List;

public class ComplexGateSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Arrays.asList("-ot");
    }

    @Override
    public String getDisplayName() {
        return "Complex gate [ATACS]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

}
