package org.workcraft.plugins.petrify.commands;

import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;

public class NetErConversionCommand extends NetConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis [Petrify with -er option]";
    }

    @Override
    public ArrayList<String> getArgs(WorkspaceEntry we) {
        ArrayList<String> args = super.getArgs(we);
        args.add("-er");
        return args;
    }

}
