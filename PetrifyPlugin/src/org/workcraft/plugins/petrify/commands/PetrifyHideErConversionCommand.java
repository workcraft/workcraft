package org.workcraft.plugins.petrify.commands;

import java.util.ArrayList;

public class PetrifyHideErConversionCommand extends PetrifyHideConversionCommand {
    @Override
    public String getDisplayName() {
        return "Net synthesis hiding selected signals and dummies [Petrify with -er option]";
    }

    @Override
    public ArrayList<String> getArgs() {
        ArrayList<String> args = super.getArgs();
        args.add("-er");
        return args;
    }
}
