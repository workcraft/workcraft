package org.workcraft.plugins.petrify.commands;

import org.workcraft.plugins.stg.StgModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.ArrayList;

public class UntoggleConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Untoggle signal transitions [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public ArrayList<String> getArgs(WorkspaceEntry we) {
        ArrayList<String> args = super.getArgs(we);
        args.add("-untog");
        return args;
    }

}
