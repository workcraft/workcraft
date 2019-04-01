package org.workcraft.plugins.atacs.commands;

public class ComplexGateSynthesisCommand extends AbstractSynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        return new String[] {"-ot"};
    }

    @Override
    public String getDisplayName() {
        return "Complex gate [ATACS]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP_MIDDLE;
    }

}
