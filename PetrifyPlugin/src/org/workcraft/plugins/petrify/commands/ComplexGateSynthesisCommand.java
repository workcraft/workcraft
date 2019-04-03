package org.workcraft.plugins.petrify.commands;

public class ComplexGateSynthesisCommand extends AbstractPetrifySynthesisCommand {

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
        return Position.TOP_MIDDLE;
    }

}
