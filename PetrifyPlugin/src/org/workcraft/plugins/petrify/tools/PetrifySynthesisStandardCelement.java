package org.workcraft.plugins.petrify.tools;

public class PetrifySynthesisStandardCelement extends PetrifySynthesis {

    @Override
    public String[] getSynthesisParameter() {
        String[] result = new String[1];
        result[0] = "-gcm";
        return result;
    }

    @Override
    public String getDisplayName() {
        return "Standard C-element [Petrify with -gcm option] (experimantal)";
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

}
