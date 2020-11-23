package org.workcraft.plugins.petrify.commands;

import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;

public class HideErConversionCommand extends HideConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis hiding selected signals and dummies [Petrify with -er option]";
    }

    @Override
    public ArrayList<String> getArgs(WorkspaceEntry we) {
        ArrayList<String> args = super.getArgs(we);
        args.add("-er");
        return args;
    }

}
