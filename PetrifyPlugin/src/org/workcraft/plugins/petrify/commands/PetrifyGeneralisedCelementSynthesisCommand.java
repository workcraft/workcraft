package org.workcraft.plugins.petrify.commands;

public class PetrifyGeneralisedCelementSynthesisCommand extends PetrifyAbstractSynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        String[] result = new String[1];
        result[0] = "-gc";
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Generalized C-element [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
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
