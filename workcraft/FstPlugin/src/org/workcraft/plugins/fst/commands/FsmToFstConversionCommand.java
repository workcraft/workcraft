package org.workcraft.plugins.fst.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.converters.FsmToFstConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class FsmToFstConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Finite State Transducer";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualFsm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        FsmToFstConverter converter = new FsmToFstConverter(me.getAs(VisualFsm.class));
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
