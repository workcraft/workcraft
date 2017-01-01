package org.workcraft.plugins.fst.commands;

public class StgToBinaryFstConversionCommand extends StgToFstConversionCommand {

    @Override
    public boolean isBinary() {
        return true;
    }

}
