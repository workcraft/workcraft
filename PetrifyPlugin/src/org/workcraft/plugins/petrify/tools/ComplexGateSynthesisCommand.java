package org.workcraft.plugins.petrify.tools;

public class ComplexGateSynthesisCommand extends AbstractPetrifySynthesisCommand {

    @Override
    public String[] getSynthesisParameter() {
        String[] result = new String[1];
        result[0] = "-cg";
        return result;
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
