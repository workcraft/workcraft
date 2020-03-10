package org.workcraft.plugins.petrify.commands;

import java.util.Collections;
import java.util.List;

public class TechnologyMappingSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Collections.singletonList("-tm");
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
