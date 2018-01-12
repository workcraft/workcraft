package org.workcraft.plugins.petrify.commands;

public class PetrifyComplexGateSynthesisCommand extends PetrifyAbstractSynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        return new String[] {"-cg"};
    }

    @Override
    public String getDisplayName() {
        return "Complex gate [Petrify]";
    }

    @Override
    public Position getPosition() {
        return null;
    }

}
