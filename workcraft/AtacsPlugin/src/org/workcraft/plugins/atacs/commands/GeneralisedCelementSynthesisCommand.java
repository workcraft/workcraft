package org.workcraft.plugins.atacs.commands;

import java.util.Arrays;
import java.util.List;

public class GeneralisedCelementSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Arrays.asList("-og");
    }

    @Override
    public String getDisplayName() {
        return "Generalised C-element [ATACS]";
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

    @Override
    public boolean sequentialAssign() {
        return true;
    }

}
