package org.workcraft.plugins.fst.commands;

import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToFsmConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        final VisualFst src = me.getAs(VisualFst.class);
        final VisualFsm dst = new VisualFsm(new Fsm());
        final FstToFsmConverter converter = new FstToFsmConverter(src, dst);
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
