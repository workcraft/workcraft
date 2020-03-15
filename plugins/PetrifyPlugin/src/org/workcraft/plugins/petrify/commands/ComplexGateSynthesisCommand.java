package org.workcraft.plugins.petrify.commands;

import java.util.Collections;
import java.util.List;

public class ComplexGateSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Collections.singletonList("-cg");
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
