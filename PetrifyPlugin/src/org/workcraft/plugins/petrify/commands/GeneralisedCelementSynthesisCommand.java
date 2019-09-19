package org.workcraft.plugins.petrify.commands;

import java.util.Arrays;
import java.util.List;

public class GeneralisedCelementSynthesisCommand extends AbstractPetrifySynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Arrays.asList("-gc");
    }

    @Override
    public String getDisplayName() {
        return "Generalised C-element [Petrify]";
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
