package org.workcraft.plugins.petrify.commands;

import java.util.ArrayList;

public class PetrifyNetErConversionCommand extends PetrifyNetConversionCommand {
    @Override
    public String getDisplayName() {
        return "Net synthesis [Petrify with -er option]";
    }

    @Override
    public ArrayList<String> getArgs() {
        ArrayList<String> args = super.getArgs();
        args.add("-er");
        return args;
    }
}
