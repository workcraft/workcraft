package org.workcraft.plugins.petrify.commands;

import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class NetConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Net synthesis [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriModel.class) || WorkspaceUtils.isApplicable(we, Fsm.class);
    }

}
