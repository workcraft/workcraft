package org.workcraft.plugins.atacs.commands;

public class AtacsStandardCelementSynthesisCommand extends AtacsAbstractSynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        return new String[] {"-os"};
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
