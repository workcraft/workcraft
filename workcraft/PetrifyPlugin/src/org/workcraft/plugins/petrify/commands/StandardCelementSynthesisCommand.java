package org.workcraft.plugins.petrify.commands;

import java.util.Collections;
import java.util.List;

public class StandardCelementSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Collections.singletonList("-mc");
    }

    @Override
    public String getDisplayName() {
        return "Standard C-element [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public boolean celementAssign() {
        return true;
    }

}
