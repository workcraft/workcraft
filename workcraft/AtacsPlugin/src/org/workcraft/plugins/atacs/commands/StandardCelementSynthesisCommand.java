package org.workcraft.plugins.atacs.commands;

import java.util.Arrays;
import java.util.List;

public class StandardCelementSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Arrays.asList("-os");
    }

    @Override
    public String getDisplayName() {
        return "Standard C-element [ATACS]";
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
