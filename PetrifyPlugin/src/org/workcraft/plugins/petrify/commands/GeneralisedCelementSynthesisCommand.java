package org.workcraft.plugins.petrify.commands;

import java.util.Collections;
import java.util.List;

public class GeneralisedCelementSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public List<String> getSynthesisParameter() {
        return Collections.singletonList("-gc");
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
