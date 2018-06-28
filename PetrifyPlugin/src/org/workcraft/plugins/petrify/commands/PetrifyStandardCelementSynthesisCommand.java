package org.workcraft.plugins.petrify.commands;

public class PetrifyStandardCelementSynthesisCommand extends PetrifyAbstractSynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        return new String[] {"-mc"};
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
    public boolean boxSequentialComponents() {
        return false;
    }

    @Override
    public boolean boxCombinationalComponents() {
        return false;
    }

    @Override
    public boolean sequentialAssign() {
        return true;
    }

}
