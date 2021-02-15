package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.converters.FsmToPetriConverter;
import org.workcraft.plugins.petri.PetriDescriptor;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualFsm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        FsmToPetriConverter converter = new FsmToPetriConverter(me.getAs(VisualFsm.class));
        return new ModelEntry(new PetriDescriptor(), converter.getDstModel());
    }

}
