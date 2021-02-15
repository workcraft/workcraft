package org.workcraft.plugins.fst.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToFsmConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class FstToFsmConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualFst.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        FstToFsmConverter converter = new FstToFsmConverter(me.getAs(VisualFst.class));
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
